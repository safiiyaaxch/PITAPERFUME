package com.scentify.service;

import com.scentify.model.Invoice;
import com.scentify.model.Order;
import com.scentify.model.Payment;
import com.scentify.repository.InvoiceRepository;
import com.scentify.repository.OrderRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class InvoiceServiceImpl implements InvoiceService {
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
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
            // ✅ Use the Invoice constructor that takes Order and Payment
            Invoice invoice = new Invoice(order, payment);
            
            // Set additional fields if needed
            invoice.setInvoiceDate(LocalDateTime.now());
            
            Invoice savedInvoice = invoiceRepository.save(invoice);
            System.out.println("✅ Invoice generated: " + savedInvoice.getInvoiceNumber() + 
                             " for Order #" + order.getOrderId());
            return savedInvoice;
            
        } catch (Exception e) {
            System.err.println("❌ Error generating invoice: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
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