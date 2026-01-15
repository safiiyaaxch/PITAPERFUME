package com.scentify.repository;

import com.scentify.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Integer> {
    Optional<Supplier> findByUser_UserId(Integer userId);
    List<Supplier> findByApprovalStatus(String approvalStatus);
}
