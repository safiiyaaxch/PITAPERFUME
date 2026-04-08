package com.scentify.repository;

import com.scentify.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProduct_ProductId(String productId);
    List<Review> findByCustomer_CustomerId(Long customerId);
    List<Review> findByReviewStatus(String reviewStatus);
}
