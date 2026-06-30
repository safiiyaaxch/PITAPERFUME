package com.scentify.controller;

import com.scentify.model.Order;
import com.scentify.model.OrderItem;
import com.scentify.model.Payment;
import com.scentify.model.Invoice;
import com.scentify.model.Product;
import com.scentify.repository.OrderRepository;
import com.scentify.repository.PaymentRepository;
import com.scentify.repository.ProductRepository;
import com.scentify.service.ToyyibPayService;
import com.scentify.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/payment")
public class PaymentController {
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    @Value("${app.payment.return-url:${app.base-url}/payment/return}")
    private String returnUrl;
    
    @Value("${app.payment.callback-url:${app.base-url}/payment/callback}")
    private String callbackUrl;
    
    @Value("${toyyibpay.sandbox-mode:false}")
    private boolean sandboxMode;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ToyyibPayService toyyibPayService;
    
    @Autowired
    private InvoiceService invoiceService;
    
    // ✅ HARDCODED PRODUCTION URL
    private static final String PRODUCTION_BASE_URL = "https://pitaperfume-production.up.railway.app";
    
    /**
     * Test endpoint to verify URLs are configured correctly
     */
    @GetMapping("/test-urls")
    @ResponseBody
    public Map<String, String> testUrls() {
        Map<String, String> urls = new HashMap<>();
        urls.put("baseUrl", baseUrl);
        urls.put("returnUrl", returnUrl);
        urls.put("callbackUrl", callbackUrl);
        urls.put("environment", sandboxMode ? "SANDBOX" : "PRODUCTION");
        urls.put("status", "OK");
        urls.put("message", "URLs are configured correctly");
        return urls;
    }
    
    /**
     * Deduct stock for all items in an order
     */
    private void deductStock(Order order) {
        if (order.isStockDeducted()) {
            System.out.println("⚠️ Stock already deducted for Order #" + order.getOrderId());
            return;
        }
        
        System.out.println("📦 DEDUCTING STOCK FOR ORDER #" + order.getOrderId());
        
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            int currentStock = product.getStock();
            int quantity = item.getQuantity();
            
            System.out.println("   Product: " + product.getProductName());
            System.out.println("   Current stock: " + currentStock);
            System.out.println("   Quantity ordered: " + quantity);
            
            if (currentStock < quantity) {
                System.err.println("❌ Not enough stock for product: " + product.getProductName());
                System.err.println("   Current stock: " + currentStock + ", Requested: " + quantity);
                throw new RuntimeException("Not enough stock for product: " + product.getProductName());
            }
            
            product.setStock(currentStock - quantity);
            productRepository.save(product);
            System.out.println("   ✅ New stock: " + product.getStock());
        }
        
        order.setStockDeducted(true);
        orderRepository.save(order);
        System.out.println("✅ Stock deduction complete for Order #" + order.getOrderId());
    }
    
    /**
     * Callback from ToyyibPay after payment
     */
    @PostMapping("/callback")
    @ResponseBody
    public String handlePaymentCallback(
            @RequestParam String refno,
            @RequestParam String status,
            @RequestParam(required = false) String reason,
            @RequestParam String billcode,
            @RequestParam(required = false) String order_id,
            @RequestParam(required = false) String amount,
            @RequestParam(required = false) String transaction_time,
            @RequestParam String hash) {
        
        try {
            System.out.println("🔔 ToyyibPay Callback received - BillCode: " + billcode + ", Status: " + status);
            
            if (!toyyibPayService.validateCallbackHash(hash, status, order_id, refno)) {
                System.err.println("❌ Invalid callback hash - rejecting request");
                return "fail";
            }
            
            Optional<Payment> paymentOpt = paymentRepository.findByBillCode(billcode);
            
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                Order order = payment.getOrder();
                
                if ("1".equals(status)) {
                    payment.setPaymentStatus("COMPLETED");
                    payment.setCompletedAt(LocalDateTime.now());
                    
                    order.setPaymentStatus("PAID");
                    order.setOrderStatus("DELIVERED");
                    order.setDeliveryDate(LocalDateTime.now());
                    order.setUpdatedAt(LocalDateTime.now());
                    
                    deductStock(order);
                    
                    System.out.println("✅ Payment confirmed for Order #" + order.getOrderId());
                    System.out.println("📦 Order automatically marked as DELIVERED");
                    System.out.println("📦 Stock deducted for all items");
                    
                    Invoice invoice = invoiceService.generateInvoice(order, payment);
                    if (invoice != null) {
                        System.out.println("✅ Invoice generated: " + invoice.getInvoiceNumber());
                    }
                    
                } else if ("2".equals(status)) {
                    payment.setPaymentStatus("PENDING");
                    order.setPaymentStatus("PENDING");
                    order.setOrderStatus("PENDING");
                    System.out.println("⏳ Payment pending for Order #" + order.getOrderId());
                    
                } else if ("3".equals(status)) {
                    payment.setPaymentStatus("FAILED");
                    order.setPaymentStatus("FAILED");
                    order.setOrderStatus("CANCELLED");
                    System.out.println("❌ Payment failed for Order #" + order.getOrderId() + " - Reason: " + reason);
                }
                
                paymentRepository.save(payment);
                orderRepository.save(order);
                return "ok";
            } else {
                System.err.println("❌ Payment not found for bill code: " + billcode);
                return "fail";
            }
        } catch (Exception e) {
            System.err.println("❌ Error processing callback: " + e.getMessage());
            e.printStackTrace();
            return "fail";
        }
    }
    
    /**
     * User returns from ToyyibPay - WITH POPUP
     */
    @GetMapping("/return")
    public String handlePaymentReturn(
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) String status_id,
            @RequestParam(required = false) String billcode,
            @RequestParam(required = false) String msg,
            HttpSession session,
            Model model,
            RedirectAttributes redirect) {
        
        try {
            System.out.println("\n" + "=".repeat(70));
            System.out.println("💳 === PAYMENT RETURN RECEIVED ===");
            System.out.println("=".repeat(70));
            System.out.println("📥 Order ID: " + orderId);
            System.out.println("📥 Status ID: " + status_id);
            System.out.println("📥 Bill Code: " + billcode);
            System.out.println("📥 Message: " + msg);
            
            if (orderId == null) {
                System.err.println("❌ No orderId provided in return URL");
                redirect.addFlashAttribute("error", "Invalid order reference");
                return "redirect:/customer/dashboard";
            }
            
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            
            if (orderOpt.isEmpty()) {
                System.err.println("❌ Order not found: " + orderId);
                redirect.addFlashAttribute("error", "Order not found");
                return "redirect:/customer/dashboard";
            }
            
            Order order = orderOpt.get();
            Optional<Payment> paymentOpt = paymentRepository.findByBillCode(billcode);
            
            boolean paymentSuccess = "1".equals(status_id);
            
            if (paymentSuccess) {
                System.out.println("✅ Payment SUCCESSFUL for Order #" + orderId);
                
                if (paymentOpt.isPresent()) {
                    Payment payment = paymentOpt.get();
                    if (!"COMPLETED".equals(payment.getPaymentStatus())) {
                        payment.setPaymentStatus("COMPLETED");
                        payment.setCompletedAt(LocalDateTime.now());
                        paymentRepository.save(payment);
                    }
                }
                
                if (!"PAID".equals(order.getPaymentStatus())) {
                    order.setPaymentStatus("PAID");
                    order.setOrderStatus("DELIVERED");
                    order.setDeliveryDate(LocalDateTime.now());
                    order.setUpdatedAt(LocalDateTime.now());
                    
                    if (!order.isStockDeducted()) {
                        deductStock(order);
                        System.out.println("📦 Stock deducted for Order #" + orderId + " (via return)");
                    }
                    
                    orderRepository.save(order);
                    System.out.println("✅ Order updated to PAID/DELIVERED");
                }
                
                Invoice existingInvoice = invoiceService.getInvoiceByOrder(order);
                Long invoiceId = null;
                
                if (existingInvoice == null && paymentOpt.isPresent()) {
                    Invoice invoice = invoiceService.generateInvoice(order, paymentOpt.get());
                    if (invoice != null) {
                        invoiceId = invoice.getInvoiceId();
                        System.out.println("✅ Invoice generated for Order #" + orderId);
                    }
                } else if (existingInvoice != null) {
                    invoiceId = existingInvoice.getInvoiceId();
                }
                
                redirect.addFlashAttribute("paymentPopup", true);
                redirect.addFlashAttribute("paymentSuccess", true);
                redirect.addFlashAttribute("paymentOrderId", orderId);
                redirect.addFlashAttribute("paymentInvoiceId", invoiceId);
                redirect.addFlashAttribute("paymentMessage", "Payment successful! Your order has been confirmed and automatically delivered. You can view your invoice in Order History.");
                
                System.out.println("✅ Flash attributes set for popup");
                
                return "redirect:/customer/dashboard";
                
            } else if ("2".equals(status_id)) {
                System.out.println("⏳ Payment PENDING for Order #" + orderId);
                
                redirect.addFlashAttribute("paymentPopup", true);
                redirect.addFlashAttribute("paymentSuccess", false);
                redirect.addFlashAttribute("paymentOrderId", orderId);
                redirect.addFlashAttribute("paymentMessage", "Your payment is being processed. Please check back later.");
                
                return "redirect:/customer/dashboard";
                
            } else if ("3".equals(status_id)) {
                System.out.println("❌ Payment FAILED for Order #" + orderId);
                
                if (paymentOpt.isPresent()) {
                    Payment payment = paymentOpt.get();
                    payment.setPaymentStatus("FAILED");
                    paymentRepository.save(payment);
                }
                order.setPaymentStatus("FAILED");
                order.setOrderStatus("CANCELLED");
                orderRepository.save(order);
                
                redirect.addFlashAttribute("paymentPopup", true);
                redirect.addFlashAttribute("paymentSuccess", false);
                redirect.addFlashAttribute("paymentOrderId", orderId);
                redirect.addFlashAttribute("paymentMessage", "Payment was not successful. Please try again.");
                
                return "redirect:/customer/dashboard";
                
            } else {
                System.out.println("⚠️ Unknown status: " + status_id);
                
                redirect.addFlashAttribute("paymentPopup", true);
                redirect.addFlashAttribute("paymentSuccess", false);
                redirect.addFlashAttribute("paymentMessage", "Payment verification pending. Please check back soon.");
                
                return "redirect:/customer/dashboard";
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error in payment return: " + e.getMessage());
            e.printStackTrace();
            
            redirect.addFlashAttribute("paymentPopup", true);
            redirect.addFlashAttribute("paymentSuccess", false);
            redirect.addFlashAttribute("paymentMessage", "An error occurred while processing your payment: " + e.getMessage());
            
            return "redirect:/customer/dashboard";
        }
    }
    
    @GetMapping("/clear-payment-popup")
    public String clearPaymentPopup(HttpSession session) {
        session.removeAttribute("paymentPopup");
        session.removeAttribute("paymentSuccess");
        session.removeAttribute("paymentOrderId");
        session.removeAttribute("paymentInvoiceId");
        session.removeAttribute("paymentMessage");
        return "redirect:/customer/dashboard";
    }
    
    @GetMapping("/invoice/order/{orderId}")
    public String viewInvoiceByOrder(@PathVariable Long orderId, Model model, RedirectAttributes redirect) {
        try {
            System.out.println("📄 === INVOICE REQUEST BY ORDER ID: " + orderId);
            
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                System.err.println("❌ Order not found: " + orderId);
                redirect.addFlashAttribute("error", "Order not found");
                return "redirect:/customer/order-history";
            }
            
            Order order = orderOpt.get();
            System.out.println("✅ Order found: #" + orderId);
            System.out.println("   Payment Status: " + order.getPaymentStatus());
            System.out.println("   Order Status: " + order.getOrderStatus());
            
            if (!"PAID".equals(order.getPaymentStatus())) {
                System.out.println("❌ Order is not paid");
                redirect.addFlashAttribute("error", "Invoice not available for unpaid orders");
                return "redirect:/customer/order-history";
            }
            
            Invoice invoice = invoiceService.getInvoiceByOrder(order);
            
            if (invoice == null) {
                System.out.println("⚠️ No invoice found for Order #" + orderId + ", generating...");
                
                Optional<Payment> paymentOpt = paymentRepository.findByOrder_OrderId(orderId);
                if (paymentOpt.isPresent()) {
                    invoice = invoiceService.generateInvoice(order, paymentOpt.get());
                    if (invoice != null) {
                        System.out.println("✅ Invoice generated: " + invoice.getInvoiceNumber());
                    } else {
                        System.err.println("❌ Failed to generate invoice");
                        redirect.addFlashAttribute("error", "Unable to generate invoice");
                        return "redirect:/customer/order-history";
                    }
                } else {
                    System.err.println("❌ No payment found for Order #" + orderId);
                    redirect.addFlashAttribute("error", "Payment record not found");
                    return "redirect:/customer/order-history";
                }
            }
            
            if (invoice == null) {
                System.err.println("❌ Invoice is null after generation");
                redirect.addFlashAttribute("error", "Unable to generate invoice");
                return "redirect:/customer/order-history";
            }
            
            System.out.println("✅ Invoice found/generated. Invoice ID: " + invoice.getInvoiceId());
            System.out.println("   Redirecting to /payment/invoice/" + invoice.getInvoiceId());
            
            return "redirect:/payment/invoice/" + invoice.getInvoiceId();
            
        } catch (Exception e) {
            System.err.println("❌ Error viewing invoice: " + e.getMessage());
            e.printStackTrace();
            redirect.addFlashAttribute("error", "Unable to load invoice: " + e.getMessage());
            return "redirect:/customer/order-history";
        }
    }

    @GetMapping("/invoice/{invoiceId}")
    public String viewInvoice(@PathVariable Long invoiceId, Model model, RedirectAttributes redirect) {
        try {
            System.out.println("📄 === VIEW INVOICE: " + invoiceId);
            
            Invoice invoice = invoiceService.getInvoiceById(invoiceId);
            if (invoice == null) {
                System.err.println("❌ Invoice not found: " + invoiceId);
                redirect.addFlashAttribute("error", "Invoice not found");
                return "redirect:/customer/order-history";
            }
            
            System.out.println("✅ Invoice found: " + invoice.getInvoiceNumber());
            System.out.println("   Order #: " + invoice.getOrder().getOrderId());
            System.out.println("   Total: RM" + invoice.getTotal());
            
            model.addAttribute("invoice", invoice);
            return "customer/invoice-view";
            
        } catch (Exception e) {
            System.err.println("❌ Error viewing invoice: " + e.getMessage());
            e.printStackTrace();
            redirect.addFlashAttribute("error", "Unable to load invoice: " + e.getMessage());
            return "redirect:/customer/order-history";
        }
    }
}