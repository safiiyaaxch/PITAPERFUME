package com.scentify.repository;

import com.scentify.model.ReviewReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewReplyRepository extends JpaRepository<ReviewReply, Long> {
    Optional<ReviewReply> findByReview_ReviewId(Long reviewId);
    List<ReviewReply> findBySupplier_SupplierId(Long supplierId);
    List<ReviewReply> findByReview_Product_ProductId(String productId);
}
