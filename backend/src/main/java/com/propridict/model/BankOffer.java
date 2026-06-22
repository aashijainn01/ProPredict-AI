package com.propridict.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bank_offers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BankOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String bank;

    @Column(nullable = false)
    private String product;

    @Column(nullable = false)
    private String customer;

    @Column(name = "loan_type", nullable = false)
    private String loanType;

    @Column(name = "rate_min", nullable = false)
    private double rateMin;

    @Column(name = "rate_max", nullable = false)
    private double rateMax;

    @Column(name = "proc_pct", nullable = false)
    private double procPct;

    @Column(name = "proc_min", nullable = false)
    private double procMin;

    @Column(name = "proc_max", nullable = false)
    private double procMax;

    @Column(name = "max_tenure", nullable = false)
    private int maxTenure;

    @Column(nullable = false)
    private String link;

    @Column(name = "last_updated")
    private String lastUpdated;
}
