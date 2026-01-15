package com.scentify.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "supplier")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "supplierid")
    private Integer supplierId;

    @OneToOne
    @JoinColumn(name = "userid", nullable = false, unique = true)
    private User user;

    @Column(name = "brandName", nullable = false, length = 100)
    private String brandName;

    @Column(name = "approvalStatus", length = 20)
    private String approvalStatus = "pending";

    @Column(name = "businessRegistration", length = 255)
    private String businessRegistration;

    @Column(name = "approvalDate")
    private LocalDateTime approvalDate;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
