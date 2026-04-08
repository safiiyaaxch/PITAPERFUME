package com.scentify.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "review")
public class Review {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;
    
    @ManyToOne
    @JoinColumn(name = "customerId", nullable = false)
    private Customer customer;
    
    @ManyToOne
    @JoinColumn(name = "productId", nullable = false)
    private Product product;
    
    private Integer rating; // 1-5 stars
    
    @Column(length = 1000)
    private String reviewText;
    
    @Column(length = 50)
    private String reviewStatus; // PENDING, APPROVED, REJECTED
    
    private Integer helpfulCount; // Number of people who found it helpful
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Constructors
    public Review() {}

    public Review(Customer customer, Product product, Integer rating, String reviewText) {
        this.customer = customer;
        this.product = product;
        this.rating = rating;
        this.reviewText = reviewText;
        this.reviewStatus = "PENDING";
        this.helpfulCount = 0;
    }

    // Getters & Setters
    public Long getReviewId() {
        return reviewId;
    }

    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public Integer getHelpfulCount() {
        return helpfulCount;
    }

    public void setHelpfulCount(Integer helpfulCount) {
        this.helpfulCount = helpfulCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Review{" +
                "reviewId=" + reviewId +
                ", customer=" + customer +
                ", product=" + product +
                ", rating=" + rating +
                ", reviewStatus='" + reviewStatus + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
