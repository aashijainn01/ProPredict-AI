package com.propridict.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PredictionResponse {

    private double baseRate;
    private double adjustedRate;
    private double totalPriceINR;
    private double totalPriceUSD;
    private double premiumFactor;
    private double premiumPct;
    private double cagr;
    private double lat;
    private double lng;

    private List<Integer> years;
    private List<Double> baseRates;
    private List<Double> adjustedRates;
}
