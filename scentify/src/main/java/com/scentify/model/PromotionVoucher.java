package com.scentify.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "promotion_voucher")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionVoucher {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "voucher_id")
    private Integer voucherId;
    
    @Column(name = "supplier_id", nullable = false)
    private Integer supplierId;
    
    @Column(name = "voucher_code", nullable = false, unique = true, length = 50)
    private String voucherCode;
    
    @Column(name = "discount_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private DiscountType discountType;  // PERCENTAGE or FIXED_AMOUNT
    
    @Column(name = "discount_value", nullable = false)
    private BigDecimal discountValue;
    
    @Column(name = "min_purchase_amount")
    private BigDecimal minPurchaseAmount;
    
    @Column(name = "start_date")
    private LocalDateTime startDate;
    
    @Column(name = "end_date")
    private LocalDateTime endDate;
    
    @Column(name = "max_usage")
    private Integer maxUsage;
    
    @Column(name = "current_usage")
    private Integer currentUsage = 0;
    
    @Column(name = "description", length = 255)
    private String description;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "promotionVoucher", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VoucherProduct> voucherProducts;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum DiscountType {
        PERCENTAGE,
        FIXED_AMOUNT
    }
    
    // Helper methods
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return isActive && 
               (startDate == null || !now.isBefore(startDate)) &&
               (endDate == null || !now.isAfter(endDate)) &&
               (maxUsage == null || currentUsage < maxUsage);
    }
    
    public boolean isExpired() {
        LocalDateTime now = LocalDateTime.now();
        return endDate != null && now.isAfter(endDate);
    }
    
    public boolean isActive() {
        return isActive != null && isActive;
    }
    
    public void incrementUsage() {
        if (currentUsage == null) {
            currentUsage = 0;
        }
        currentUsage++;
    }
}
