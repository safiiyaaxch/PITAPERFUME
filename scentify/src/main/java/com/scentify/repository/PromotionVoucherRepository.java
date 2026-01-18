package com.scentify.repository;

import com.scentify.model.PromotionVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionVoucherRepository extends JpaRepository<PromotionVoucher, Integer> {
    
    Optional<PromotionVoucher> findByVoucherCode(String voucherCode);
    
    List<PromotionVoucher> findBySupplierId(Integer supplierId);
    
    List<PromotionVoucher> findBySupplierIdAndIsActive(Integer supplierId, Boolean isActive);
}
