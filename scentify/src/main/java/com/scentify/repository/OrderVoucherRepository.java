package com.scentify.repository;

import com.scentify.model.OrderVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderVoucherRepository extends JpaRepository<OrderVoucher, Integer> {
    
    Optional<OrderVoucher> findByOrderId(Integer orderId);
    
    List<OrderVoucher> findByCustomerId(Integer customerId);
    
    List<OrderVoucher> findByVoucherId(Integer voucherId);
}
