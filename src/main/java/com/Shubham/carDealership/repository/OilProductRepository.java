package com.Shubham.carDealership.repository;

import com.Shubham.carDealership.model.OilProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OilProductRepository extends JpaRepository<OilProduct, Long> {
    List<OilProduct> findByCategory(String category);
    List<OilProduct> findByAvailable(Boolean available);
}