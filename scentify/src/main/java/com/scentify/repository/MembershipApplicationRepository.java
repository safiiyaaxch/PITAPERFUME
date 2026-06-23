package com.scentify.repository;

import com.scentify.model.MembershipApplication;
import com.scentify.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipApplicationRepository extends JpaRepository<MembershipApplication, Integer> {
    
    Optional<MembershipApplication> findByCustomer(Customer customer);
    
    List<MembershipApplication> findByStatus(String status);
    
    List<MembershipApplication> findByStatusOrderByAppliedDateDesc(String status);
    
    // ✅ ADD THIS - Forces loading of customer and user data
    @Query("SELECT a FROM MembershipApplication a " +
           "LEFT JOIN FETCH a.customer c " +
           "LEFT JOIN FETCH c.user " +
           "WHERE a.applicationId = :id")
    Optional<MembershipApplication> findByIdWithCustomer(@Param("id") Integer id);
}