package com.scentify.repository;

import com.scentify.model.ShoppingCart;
import com.scentify.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
    Optional<ShoppingCart> findByCustomer_CustomerId(Long customerId);
    Optional<ShoppingCart> findByCustomer(Customer customer);
}
