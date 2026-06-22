package com.propridict.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EMIResponse {

    private double loanPrincipal;
    private double downPayment;
    private double monthlyEMI;
    private double totalInterest;
    private double totalPayment;
}
