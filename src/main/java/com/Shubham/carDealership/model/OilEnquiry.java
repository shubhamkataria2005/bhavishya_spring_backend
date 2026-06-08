package com.Shubham.carDealership.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "oil_enquiries")
@Data
public class OilEnquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false)
    private String name;

    private String company;

    @Column(nullable = false)
    private String phone;

    private String email;
    private String city;
    private String state;

    @Column(name = "order_type")
    private String orderType; // retail, wholesale, distributor

    @Column(columnDefinition = "TEXT")
    private String message;

    private String status = "PENDING"; // PENDING, CONTACTED, CLOSED

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}