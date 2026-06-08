package com.Shubham.carDealership.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "oil_products")
@Data
public class OilProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String size;

    private String weight;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    private String usp;

    private String category; // retail or wholesale

    @Column(columnDefinition = "TEXT")
    private String description;

    private Boolean available = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}