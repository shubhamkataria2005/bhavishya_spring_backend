package com.Shubham.carDealership.repository;

import com.Shubham.carDealership.model.OilOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OilOrderRepository extends JpaRepository<OilOrder, Long> {
    List<OilOrder> findByUserId(Long userId);
    List<OilOrder> findByStatus(String status);
}