package com.scentify.repository;

import com.scentify.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomer_CustomerId(Long customerId);
    List<Order> findByProduct_ProductId(String productId);
    List<Order> findByOrderStatus(String orderStatus);
}
