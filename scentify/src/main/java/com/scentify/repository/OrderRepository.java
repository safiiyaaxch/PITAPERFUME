package com.scentify.repository;

import com.scentify.model.Customer;
import com.scentify.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // ===== FIND BY CUSTOMER =====
    List<Order> findByCustomer_CustomerId(Long customerId);
    
    List<Order> findByCustomer(Customer customer);
    
    // ===== FIND BY STATUS =====
    List<Order> findByPaymentStatus(String paymentStatus);
    
    List<Order> findByOrderStatus(String orderStatus);
    
    // ===== FIND BY PAYMENT AND ORDER STATUS =====
    List<Order> findByPaymentStatusAndOrderStatus(String paymentStatus, String orderStatus);
    

    // ===== FIND ORDERS CONTAINING A PRODUCT (Multiple items support) =====
    // Find all orders containing a specific product
    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi WHERE oi.product.productId = :productId")
    List<Order> findOrdersContainingProduct(@Param("productId") String productId);
    
    // Find orders containing a specific product for a specific customer
    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi WHERE o.customer.customerId = :customerId AND oi.product.productId = :productId")
    List<Order> findOrdersByCustomerAndProduct(@Param("customerId") Long customerId, @Param("productId") String productId);
    
    // Find PAID orders containing a specific product
    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi WHERE o.paymentStatus = 'PAID' AND oi.product.productId = :productId")
    List<Order> findPaidOrdersContainingProduct(@Param("productId") String productId);
    
    // Find DELIVERED orders containing a specific product
    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi WHERE o.orderStatus = 'DELIVERED' AND oi.product.productId = :productId")
    List<Order> findDeliveredOrdersContainingProduct(@Param("productId") String productId);
    
    // ===== FIND BY DATE =====
    List<Order> findByOrderDateBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);

 @Query("SELECT o FROM Order o WHERE o.customer.customerId = :customerId")
    List<Order> findOrdersByCustomerId(@Param("customerId") Long customerId);
}