package com.propridict.controller;

import com.propridict.dto.PredictionRequest;
import com.propridict.dto.PredictionResponse;
import com.propridict.service.PredictionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;

    @PostMapping("/predict")
    public ResponseEntity<PredictionResponse> predict(@Valid @RequestBody PredictionRequest request) {
        PredictionResponse response = predictionService.predict(request);
        return ResponseEntity.ok(response);
    }
}
