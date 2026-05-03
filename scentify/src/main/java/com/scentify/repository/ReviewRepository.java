package com.scentify.repository;

import com.scentify.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    @Query("SELECT r FROM Review r LEFT JOIN FETCH r.reply WHERE r.product.productId = :productId")
    List<Review> findByProduct_ProductId(@Param("productId") String productId);
    
    List<Review> findByCustomer_CustomerId(Long customerId);
    List<Review> findByReviewStatus(String reviewStatus);
    
    @Query("SELECT r FROM Review r LEFT JOIN FETCH r.reply WHERE r.product.userId = :userId")
    List<Review> findBySupplierUserId(@Param("userId") Integer userId);
}

