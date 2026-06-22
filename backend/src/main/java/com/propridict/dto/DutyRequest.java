package com.propridict.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DutyRequest {

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Owner type is required")
    private String ownerType;

    @Min(value = 0, message = "Property value cannot be negative")
    private double propertyValue;
}
