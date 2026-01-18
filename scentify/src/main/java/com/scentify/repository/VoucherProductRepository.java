package com.scentify.repository;

import com.scentify.model.VoucherProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VoucherProductRepository extends JpaRepository<VoucherProduct, Integer> {
    
    List<VoucherProduct> findByPromotionVoucher_VoucherId(Integer voucherId);
    
    List<VoucherProduct> findByProductId(String productId);
    
    void deleteByPromotionVoucher_VoucherId(Integer voucherId);
}
