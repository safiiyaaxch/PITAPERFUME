package com.scentify.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_voucher")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderVoucher {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_voucher_id")
    private Integer orderVoucherId;
    
    @Column(name = "order_id", nullable = false)
    private Integer orderId;
    
    @Column(name = "voucher_id", nullable = false)
    private Integer voucherId;
    
    @Column(name = "customer_id", nullable = false)
    private Integer customerId;
    
    @Column(name = "discount_amount", nullable = false)
    private BigDecimal discountAmount;
    
    @Column(name = "used_date")
    private LocalDateTime usedDate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
