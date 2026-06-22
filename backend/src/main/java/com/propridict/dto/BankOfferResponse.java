package com.propridict.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BankOfferResponse {

    private String bank;
    private String product;
    private String customer;
    private String loanType;
    private double rateMin;
    private double rateMax;
    private double procPct;
    private double procMin;
    private double procMax;
    private int maxTenure;
    private String link;
    private String lastUpdated;

    // Computed fields
    private int tenureUsed;
    private double emiMin;
    private double processingFee;
}
