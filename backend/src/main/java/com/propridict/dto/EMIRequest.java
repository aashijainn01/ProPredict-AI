package com.propridict.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EMIRequest {

    @Min(value = 0, message = "Total price cannot be negative")
    private double totalPrice;

    @Min(value = 0, message = "Interest rate cannot be negative")
    @Max(value = 100, message = "Interest rate cannot exceed 100%")
    private double interestRate;

    @Min(value = 1, message = "Tenure must be at least 1 year")
    @Max(value = 40, message = "Tenure cannot exceed 40 years")
    private int tenure;

    @Min(value = 0, message = "Down payment % cannot be negative")
    @Max(value = 100, message = "Down payment % cannot exceed 100%")
    private double downPaymentPct;
}
