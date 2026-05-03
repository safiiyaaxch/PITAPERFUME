package com.scentify.repository;

import com.scentify.model.Payment;
import com.scentify.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBillCode(String billCode);
    Optional<Payment> findByOrder(Order order);
}
