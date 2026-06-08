package com.Shubham.carDealership.repository;

import com.Shubham.carDealership.model.OilEnquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OilEnquiryRepository extends JpaRepository<OilEnquiry, Long> {
    List<OilEnquiry> findByStatus(String status);
    List<OilEnquiry> findByUserId(Long userId);
}