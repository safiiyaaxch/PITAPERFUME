package com.scentify.repository;

import com.scentify.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String> {
    // Find all products by a specific supplier (userID)
    List<Product> findByUserId(Integer userId);
    
    // Find products by supplier and category
    List<Product> findByUserIdAndCategoryId(Integer userId, String categoryId);
    
    // Find products with pending approval for a supplier
    List<Product> findByUserIdAndApprovalStatus(Integer userId, String approvalStatus);
    
    // Search products by name for a specific supplier
    List<Product> findByUserIdAndProductNameContainingIgnoreCase(Integer userId, String productName);
}