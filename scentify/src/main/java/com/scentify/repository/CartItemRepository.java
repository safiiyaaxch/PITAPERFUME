package com.scentify.repository;

import com.scentify.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCart_CartId(Long cartId);
    Optional<CartItem> findByCart_CartIdAndProduct_ProductId(Long cartId, String productId);
    void deleteByCart_CartId(Long cartId);
}
