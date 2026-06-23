package com.scentify.repository;

import com.scentify.model.Invoice;
import com.scentify.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    Optional<Invoice> findByOrder(Order order);
    
    @Query("SELECT i FROM Invoice i WHERE i.order.orderId = :orderId")
    Optional<Invoice> findByOrder_OrderId(@Param("orderId") Long orderId);
    
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
}