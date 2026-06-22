package com.propridict.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "property_rates")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PropertyRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String landmark;

    @Column(nullable = false)
    private String category;

    @Column(name = "rate_2018", nullable = false)
    private double rate2018;

    @Column(name = "rate_2024", nullable = false)
    private double rate2024;

    @Column(nullable = false)
    private double lat;

    @Column(nullable = false)
    private double lng;
}
