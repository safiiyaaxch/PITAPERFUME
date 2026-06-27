package com.scentify.controller;

import com.scentify.model.Order;
import com.scentify.model.Payment;
import com.scentify.model.Invoice;
import com.scentify.repository.OrderRepository;
import com.scentify.repository.PaymentRepository;
import com.scentify.service.ToyyibPayService;
import com.scentify.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/payment")
public class PaymentController {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private ToyyibPayService toyyibPayService;
    
    @Autowired
    private InvoiceService invoiceService;
    
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
                    // ✅ Payment successful - AUTO-DELIVER
                    payment.setPaymentStatus("COMPLETED");
                    payment.setCompletedAt(LocalDateTime.now());
                    
                    order.setPaymentStatus("PAID");
                    order.setOrderStatus("DELIVERED");  // 🚀 Auto-deliver!
                    order.setDeliveryDate(LocalDateTime.now());  // Set delivery date to now
                    order.setUpdatedAt(LocalDateTime.now());
                    
                    System.out.println("✅ Payment confirmed for Order #" + order.getOrderId());
                    System.out.println("📦 Order automatically marked as DELIVERED");
                    
                    // Generate invoice
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
        
        // Check if payment was successful
        // ToyyibPay status: "1" = success, "2" = pending, "3" = failed
        boolean paymentSuccess = "1".equals(status_id);
        
        if (paymentSuccess) {
            System.out.println("✅ Payment SUCCESSFUL for Order #" + orderId);
            
            // Update payment if exists
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                if (!"COMPLETED".equals(payment.getPaymentStatus())) {
                    payment.setPaymentStatus("COMPLETED");
                    payment.setCompletedAt(LocalDateTime.now());
                    paymentRepository.save(payment);
                }
            }
            
            // Update order
            if (!"PAID".equals(order.getPaymentStatus())) {
                order.setPaymentStatus("PAID");
                order.setOrderStatus("DELIVERED");
                order.setDeliveryDate(LocalDateTime.now());
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
                System.out.println("✅ Order updated to PAID/DELIVERED");
            }
            
            // Generate or get invoice
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
            
            // ✅ Store payment success info in session for popup
            session.setAttribute("paymentPopup", true);
            session.setAttribute("paymentSuccess", true);
            session.setAttribute("paymentOrderId", orderId);
            session.setAttribute("paymentInvoiceId", invoiceId);
            session.setAttribute("paymentMessage", "🎉 Payment successful! Your order has been confirmed and automatically delivered. You can view your invoice in Order History.");
            
            return "redirect:/customer/dashboard";
            
        } else if ("2".equals(status_id)) {
            // Payment pending
            System.out.println("⏳ Payment PENDING for Order #" + orderId);
            
            session.setAttribute("paymentPopup", true);
            session.setAttribute("paymentSuccess", false);
            session.setAttribute("paymentOrderId", orderId);
            session.setAttribute("paymentMessage", "⏳ Your payment is being processed. Please check back later.");
            
            return "redirect:/customer/dashboard";
            
        } else if ("3".equals(status_id)) {
            // Payment failed
            System.out.println("❌ Payment FAILED for Order #" + orderId);
            
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                payment.setPaymentStatus("FAILED");
                paymentRepository.save(payment);
            }
            order.setPaymentStatus("FAILED");
            order.setOrderStatus("CANCELLED");
            orderRepository.save(order);
            
            session.setAttribute("paymentPopup", true);
            session.setAttribute("paymentSuccess", false);
            session.setAttribute("paymentOrderId", orderId);
            session.setAttribute("paymentMessage", "❌ Payment was not successful. Please try again.");
            
            return "redirect:/customer/dashboard";
            
        } else {
            // Unknown status
            System.out.println("⚠️ Unknown status: " + status_id);
            
            session.setAttribute("paymentPopup", true);
            session.setAttribute("paymentSuccess", false);
            session.setAttribute("paymentMessage", "⚠️ Payment verification pending. Please check back soon.");
            
            return "redirect:/customer/dashboard";
        }
        
    } catch (Exception e) {
        System.err.println("❌ Error in payment return: " + e.getMessage());
        e.printStackTrace();
        
        session.setAttribute("paymentPopup", true);
        session.setAttribute("paymentSuccess", false);
        session.setAttribute("paymentMessage", "An error occurred while processing your payment: " + e.getMessage());
        
        return "redirect:/customer/dashboard";
    }
}
    /**
     * Clear payment popup after it's been shown
     */
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
        
        // Check if order is paid
        if (!"PAID".equals(order.getPaymentStatus())) {
            System.out.println("❌ Order is not paid");
            redirect.addFlashAttribute("error", "Invoice not available for unpaid orders");
            return "redirect:/customer/order-history";
        }
        
        // Try to find existing invoice
        Invoice invoice = invoiceService.getInvoiceByOrder(order);
        
        if (invoice == null) {
            System.out.println("⚠️ No invoice found for Order #" + orderId + ", generating...");
            
            // Try to find payment
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
        
        // Redirect to the actual invoice view with invoice ID
        return "redirect:/payment/invoice/" + invoice.getInvoiceId();
        
    } catch (Exception e) {
        System.err.println("❌ Error viewing invoice: " + e.getMessage());
        e.printStackTrace();
        redirect.addFlashAttribute("error", "Unable to load invoice: " + e.getMessage());
        return "redirect:/customer/order-history";
    }
}

/**
 * View invoice by Invoice ID
 */
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
