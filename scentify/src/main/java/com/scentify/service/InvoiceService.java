package com.scentify.service;

import com.scentify.model.Invoice;
import com.scentify.model.Order;
import com.scentify.model.Payment;

public interface InvoiceService {
    
    Invoice generateInvoice(Order order, Payment payment);
    
    Invoice getInvoiceByOrder(Order order);
    
    Invoice getInvoiceById(Long invoiceId);
    
    Invoice getInvoiceByOrderId(Long orderId);
}