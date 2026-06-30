package com.scentify.service;

import com.scentify.model.Invoice;
import com.scentify.model.Order;
import com.scentify.model.OrderItem;
import com.scentify.model.Payment;
import com.scentify.model.OrderVoucher;
import com.scentify.repository.InvoiceRepository;
import com.scentify.repository.OrderRepository;
import com.scentify.repository.OrderVoucherRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class InvoiceServiceImpl implements InvoiceService {
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderVoucherRepository orderVoucherRepository;
    
    @Override
    @Transactional
    public Invoice generateInvoice(Order order, Payment payment) {
        if (order == null || payment == null) {
            System.err.println("❌ Cannot generate invoice: Order or Payment is null");
            return null;
        }
        
        // Check if invoice already exists
        Invoice existingInvoice = getInvoiceByOrder(order);
        if (existingInvoice != null) {
            System.out.println("⚠️ Invoice already exists for Order #" + order.getOrderId());
            return existingInvoice;
        }
        
        try {
            // ✅ Calculate invoice details
            String invoiceNumber = generateInvoiceNumber(order);
            LocalDateTime invoiceDate = LocalDateTime.now();
            
            // ✅ Calculate subtotal from order items
            BigDecimal subtotal = order.getOrderItems().stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // ✅ Calculate discount from order vouchers
            BigDecimal discount = BigDecimal.ZERO;
            try {
                List<OrderVoucher> orderVouchers = orderVoucherRepository.findByOrderId(order.getOrderId().intValue());
                for (OrderVoucher ov : orderVouchers) {
                    if (ov.getDiscountAmount() != null) {
                        discount = discount.add(ov.getDiscountAmount());
                    }
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error fetching vouchers: " + e.getMessage());
            }
            
            // ✅ If no voucher found, calculate discount from order total difference
            if (discount.compareTo(BigDecimal.ZERO) == 0) {
                BigDecimal orderTotal = order.getTotalPrice();
                if (subtotal.compareTo(orderTotal) > 0) {
                    discount = subtotal.subtract(orderTotal);
                    System.out.println("📊 Calculated discount from order total difference: RM" + discount);
                }
            }
            
            // ✅ Tax - set to 0% since you don't use tax
            BigDecimal taxRate = BigDecimal.valueOf(0.00);
            BigDecimal tax = BigDecimal.ZERO;
            
            // ✅ Total = subtotal - discount + tax
            BigDecimal total = subtotal.subtract(discount).add(tax);
            
            System.out.println("   Invoice calculation for Order #" + order.getOrderId());
            System.out.println("   Subtotal: RM" + subtotal);
            System.out.println("   Discount: RM" + discount);
            System.out.println("   Tax (0%): RM" + tax);
            System.out.println("   Total: RM" + total);
            
            // Create invoice using the constructor
            Invoice invoice = new Invoice(order, payment);
            invoice.setInvoiceNumber(invoiceNumber);
            invoice.setInvoiceDate(invoiceDate);
            invoice.setSubtotal(subtotal);
            invoice.setDiscount(discount);
            invoice.setTax(tax);
            invoice.setTaxRate("0%");
            invoice.setTotal(total);
            invoice.setPaymentMethod("ToyyibPay");
            invoice.setPaymentReference(payment.getBillCode());
            
            Invoice savedInvoice = invoiceRepository.save(invoice);
            System.out.println("✅ Invoice generated: " + savedInvoice.getInvoiceNumber() + 
                             " for Order #" + order.getOrderId());
            System.out.println("   Total: RM" + savedInvoice.getTotal());
            return savedInvoice;
            
        } catch (Exception e) {
            System.err.println("❌ Error generating invoice: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private String generateInvoiceNumber(Order order) {
        LocalDateTime now = LocalDateTime.now();
        String year = String.valueOf(now.getYear());
        String month = String.format("%02d", now.getMonthValue());
        String day = String.format("%02d", now.getDayOfMonth());
        // Add a random element to ensure uniqueness
        int random = (int) (Math.random() * 1000);
        return "INV-" + year + month + day + "-" + order.getOrderId() + "-" + String.format("%03d", random);
    }
    
    @Override
    public Invoice getInvoiceByOrder(Order order) {
        if (order == null) {
            return null;
        }
        try {
            return invoiceRepository.findByOrder(order).orElse(null);
        } catch (Exception e) {
            System.err.println("❌ Error finding invoice by order: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public Invoice getInvoiceById(Long invoiceId) {
        if (invoiceId == null) {
            return null;
        }
        try {
            return invoiceRepository.findById(invoiceId).orElse(null);
        } catch (Exception e) {
            System.err.println("❌ Error finding invoice by ID: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public Invoice getInvoiceByOrderId(Long orderId) {
        if (orderId == null) {
            return null;
        }
        try {
            return invoiceRepository.findByOrder_OrderId(orderId).orElse(null);
        } catch (Exception e) {
            System.err.println("❌ Error finding invoice by order ID: " + e.getMessage());
            return null;
        }
    }
}