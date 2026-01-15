package com.scentify.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "product")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    @Id
    @Column(name = "productid", columnDefinition = "char(3)")
    private String productId;  
    
    @Column(name = "userid", nullable = false)
    private Integer userId; 
    
    @Column(name = "categoryid", length = 3, nullable = false)
    private String categoryId;
    
    @Column(name = "productName", nullable = false, length = 100)
    private String productName;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "prodimage", nullable = false, length = 200)
    private String prodimage;  // Image URL or path

    @Column(name = "price")
    private Double price = 0.0;
    
    @Column(name = "stock")
    private Integer stock = 0;
    
    @Column(name = "approvalStatus", length = 20)
    private String approvalStatus = "pending";  // pending, approved, rejected
    
    @Column(name = "createdAt")
    private LocalDateTime createdAt;
    
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;
    
    // Pre-persist callback to set timestamps
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Helper methods
    public void increaseStock(int quantity) {
        this.stock += quantity;
    }
    
    public void decreaseStock(int quantity) {
        if (this.stock >= quantity) {
            this.stock -= quantity;
        } else {
            throw new IllegalArgumentException("Insufficient stock");
        }
    }
    
    public boolean isOutOfStock() {
        return stock <= 0;
    }
    
    public boolean isApproved() {
        return "approved".equalsIgnoreCase(approvalStatus);
    }
    
    public boolean isPending() {
        return "pending".equalsIgnoreCase(approvalStatus);
    }
    
    public boolean isRejected() {
        return "rejected".equalsIgnoreCase(approvalStatus);
    }
}