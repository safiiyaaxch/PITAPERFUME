package com.scentify.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "membership_application")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MembershipApplication {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Integer applicationId;
    
    @OneToOne
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    private Customer customer;
    
    @Column(name = "application_status", nullable = false, length = 20)
    private String status; // PENDING, APPROVED, REJECTED
    
    @Column(name = "terms_accepted", nullable = false)
    private Boolean termsAccepted;
    
    @Column(name = "applied_date", nullable = false)
    private LocalDateTime appliedDate;
    
    @Column(name = "approved_date")
    private LocalDateTime approvedDate;
    
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;
    
    @Column(name = "notes", length = 500)
    private String notes;
    
    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = "PENDING";
        }
        if (this.appliedDate == null) {
            this.appliedDate = LocalDateTime.now();
        }
        if (this.termsAccepted == null) {
            this.termsAccepted = false;
        }
    }
}
