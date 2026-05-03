package com.scentify.repository;

import com.scentify.model.MembershipApplication;
import com.scentify.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipApplicationRepository extends JpaRepository<MembershipApplication, Integer> {
    
    Optional<MembershipApplication> findByCustomer(Customer customer);
    
    List<MembershipApplication> findByStatus(String status);
    
    List<MembershipApplication> findByStatusOrderByAppliedDateDesc(String status);
}
