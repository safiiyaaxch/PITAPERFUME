package com.scentify.repository;

import com.scentify.model.Review;
import com.scentify.model.ReviewReply;
import com.scentify.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewReplyRepository extends JpaRepository<ReviewReply, Long> {
    
    // Find reply by review ID
    Optional<ReviewReply> findByReview_ReviewId(Long reviewId);
    
    // Find reply by review (alternative method)
    Optional<ReviewReply> findByReview(Review review);
    
    // Find all replies by supplier ID
    List<ReviewReply> findBySupplier_SupplierId(Long supplierId);
    
    // Find all replies by supplier (using Supplier object)
    List<ReviewReply> findBySupplier(Supplier supplier);
    
    // Find replies by product ID (through review)
    List<ReviewReply> findByReview_Product_ProductId(String productId);
    
    // ✅ ADD THIS - Check if a review already has a reply
    boolean existsByReview_ReviewId(Long reviewId);
    
    // ✅ ADD THIS - Find replies by supplier with reviews eagerly loaded
    @Query("SELECT rr FROM ReviewReply rr LEFT JOIN FETCH rr.review WHERE rr.supplier.supplierId = :supplierId")
    List<ReviewReply> findBySupplierWithReviews(@Param("supplierId") Long supplierId);
    
    // ✅ ADD THIS - Delete reply by review ID
    void deleteByReview_ReviewId(Long reviewId);
}