package com.scentify.repository;

import com.scentify.model.Customer;
import com.scentify.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    // Find reviews by product ID with replies eagerly loaded
    @Query("SELECT r FROM Review r LEFT JOIN FETCH r.reply WHERE r.product.productId = :productId")
    List<Review> findByProduct_ProductId(@Param("productId") String productId);
    
    // Find reviews by customer ID
    List<Review> findByCustomer_CustomerId(Long customerId);
    
    // ✅ ADD THIS - Find review by customer and product (for duplicate check)
    @Query("SELECT r FROM Review r WHERE r.customer = :customer AND r.product.productId = :productId")
    Optional<Review> findByCustomerAndProduct_ProductId(@Param("customer") Customer customer, @Param("productId") String productId);
    
    // Find reviews by review status
    List<Review> findByReviewStatus(String reviewStatus);
    
    // Find reviews by product user ID (supplier) with replies eagerly loaded
    @Query("SELECT r FROM Review r LEFT JOIN FETCH r.reply WHERE r.product.userId = :userId")
    List<Review> findBySupplierUserId(@Param("userId") Long userId);  // ← FIXED: Changed Integer to Long
    
    // ✅ ADD THIS - Count reviews by product ID
    long countByProduct_ProductId(String productId);
    
    // ✅ ADD THIS - Get average rating for a product
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.productId = :productId")
    Double getAverageRatingByProductId(@Param("productId") String productId);
    
    // ✅ ADD THIS - Get rating distribution for a product
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.product.productId = :productId GROUP BY r.rating ORDER BY r.rating DESC")
    List<Object[]> getRatingDistributionByProductId(@Param("productId") String productId);

    
}