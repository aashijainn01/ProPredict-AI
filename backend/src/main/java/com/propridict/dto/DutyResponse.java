package com.propridict.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DutyResponse {

    private String state;
    private String ownerType;
    private double stampDutyPct;
    private double registrationPct;
    private double stampDutyAmount;
    private double registrationAmount;
    private double totalFees;
    private double allInCost;
}
