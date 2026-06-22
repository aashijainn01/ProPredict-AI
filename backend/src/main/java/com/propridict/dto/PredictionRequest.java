package com.propridict.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PredictionRequest {

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Landmark is required")
    private String landmark;

    @NotBlank(message = "Category is required")
    private String category;

    @Min(value = 1, message = "Size must be at least 1 sq ft")
    @Max(value = 1000000, message = "Size cannot exceed 1,000,000 sq ft")
    private int size;

    @Min(value = 2025, message = "Prediction year must be at least 2025")
    @Max(value = 2050, message = "Prediction year cannot exceed 2050")
    private int year;

    @Min(value = 0, message = "Road width cannot be negative")
    @Max(value = 200, message = "Road width cannot exceed 200 ft")
    private double roadWidth;

    @Min(value = 0, message = "Frontage cannot be negative")
    @Max(value = 300, message = "Frontage cannot exceed 300 ft")
    private double frontage;

    private boolean cornerPlot;
}
