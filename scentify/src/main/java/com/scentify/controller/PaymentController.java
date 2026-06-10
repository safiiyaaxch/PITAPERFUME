package com.scentify.controller;

import com.scentify.model.Order;
import com.scentify.model.Payment;
import com.scentify.repository.OrderRepository;
import com.scentify.repository.PaymentRepository;
import com.scentify.service.ToyyibPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    
    /**
     * Callback from ToyyibPay after payment
     * Reference: https://toyyibpay.com/apireference/
     * 
     * Callback Parameters (POST):
     * - refno: Payment reference number
     * - status: 1=success, 2=pending, 3=fail
     * - reason: Reason for status
     * - billcode: Bill code
     * - order_id: External reference number
     * - amount: Payment amount
     * - transaction_time: Transaction datetime
     * - hash: MD5 hash for verification
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
            
            // Validate callback hash first
            if (!toyyibPayService.validateCallbackHash(hash, status, order_id, refno)) {
                System.err.println("❌ Invalid callback hash - rejecting request");
                return "fail"; // Reject invalid request
            }
            
            Optional<Payment> paymentOpt = paymentRepository.findByBillCode(billcode);
            
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                Order order = payment.getOrder();
                
                // Status: 1=success, 2=pending, 3=fail
                if ("1".equals(status)) {
                    // Payment successful
                    payment.setPaymentStatus("COMPLETED");
                    payment.setCompletedAt(LocalDateTime.now());
                    order.setPaymentStatus("PAID");
                    order.setOrderStatus("CONFIRMED");
                    System.out.println("✅ Payment confirmed for Order #" + order.getOrderId());
                } else if ("2".equals(status)) {
                    // Payment pending
                    payment.setPaymentStatus("PENDING");
                    order.setPaymentStatus("PENDING");
                    System.out.println("⏳ Payment pending for Order #" + order.getOrderId());
                } else if ("3".equals(status)) {
                    // Payment failed
                    payment.setPaymentStatus("FAILED");
                    order.setPaymentStatus("FAILED");
                    System.out.println("❌ Payment failed for Order #" + order.getOrderId() + " - Reason: " + reason);
                }
                
                paymentRepository.save(payment);
                orderRepository.save(order);
                return "ok"; // Acknowledge receipt
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
     * User returns from ToyyibPay
     * Reference: https://toyyibpay.com/apireference/
     * 
     * Return URL Parameters (GET):
     * - status_id: 1=success, 2=pending, 3=fail
     * - billcode: Bill code
     * - order_id: External reference number
     */
    @GetMapping("/return")
    public String handlePaymentReturn(
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) String status_id,
            @RequestParam(required = false) String billcode,
            RedirectAttributes redirect) {
        
        try {
            System.out.println("↩️  Payment Return - OrderId: " + orderId + ", Status: " + status_id + ", BillCode: " + billcode);
            
            if (orderId == null) {
                redirect.addFlashAttribute("error", "Invalid order reference");
                return "redirect:/customer/dashboard";
            }
            
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                Optional<Payment> paymentOpt = paymentRepository.findByBillCode(billcode);
                
                // Status: 1=success, 2=pending, 3=fail
                if ("1".equals(status_id)) {
                    // Payment successful - UPDATE database if needed
                    if (paymentOpt.isPresent()) {
                        Payment payment = paymentOpt.get();
                        if (!"COMPLETED".equals(payment.getPaymentStatus())) {
                            System.out.println("💾 Updating payment status to COMPLETED for bill code: " + billcode);
                            payment.setPaymentStatus("COMPLETED");
                            payment.setCompletedAt(LocalDateTime.now());
                            paymentRepository.save(payment);
                        }
                    }
                    
                    // Update order
                    if (!"PAID".equals(order.getPaymentStatus())) {
                        System.out.println("💾 Updating order payment status to PAID for order #" + orderId);
                        order.setPaymentStatus("PAID");
                        order.setOrderStatus("CONFIRMED");
                        orderRepository.save(order);
                    }
                    
                    redirect.addFlashAttribute("success", "Payment successful! Your order has been confirmed. Order #" + orderId);
                    System.out.println("✅ Payment return - Order #" + orderId + " confirmed");
                    return "redirect:/customer/dashboard";
                    
                } else if ("2".equals(status_id)) {
                    redirect.addFlashAttribute("info", "Payment is being processed. Please check back later. Order #" + orderId);
                    System.out.println("⏳ Payment return - Order #" + orderId + " pending");
                    return "redirect:/customer/dashboard";
                    
                } else if ("3".equals(status_id)) {
                    // Payment failed - update status
                    if (paymentOpt.isPresent()) {
                        Payment payment = paymentOpt.get();
                        payment.setPaymentStatus("FAILED");
                        paymentRepository.save(payment);
                    }
                    order.setPaymentStatus("FAILED");
                    orderRepository.save(order);
                    
                    redirect.addFlashAttribute("error", "Payment failed. Please try again. Order #" + orderId);
                    System.out.println("❌ Payment return - Order #" + orderId + " failed");
                    return "redirect:/customer/cart";
                } else {
                    redirect.addFlashAttribute("error", "Payment verification pending. Please check back soon.");
                    return "redirect:/customer/dashboard";
                }
            }
            
            redirect.addFlashAttribute("error", "Order not found");
            
        } catch (Exception e) {
            System.err.println("❌ Error in payment return: " + e.getMessage());
            e.printStackTrace();
            redirect.addFlashAttribute("error", "An error occurred while processing your payment");
        }
        
        return "redirect:/customer/dashboard";
    }
}
