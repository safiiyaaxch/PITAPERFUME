package com.scentify.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customerid")
    private Integer customerId;
    
    @OneToOne
    @JoinColumn(name = "userid", nullable = false)
    private User user;
    
    @Column(name = "fullname", nullable = false, length = 100)
    private String fullname;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "address", length = 255)
    private String address;
    
    @Column(name = "city", length = 50)
    private String city;
    
    @Column(name = "country", length = 50)
    private String country;
    
    @Column(name = "preferred_scent_type", length = 100)
    private String preferredScentType;
    
    @Column(name = "loyalty_points")
    private Integer loyaltyPoints = 0;
    
    @Column(name = "is_member")
    private Boolean isMember = false;  // false = non-member, true = member
    
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedAt;
    
    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}