package com.propridict.controller;

import com.propridict.service.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PredictionService predictionService;

    @GetMapping("/cities")
    public ResponseEntity<List<String>> getCities() {
        return ResponseEntity.ok(predictionService.getCities());
    }

    @GetMapping("/landmarks")
    public ResponseEntity<List<String>> getLandmarks(@RequestParam String city) {
        return ResponseEntity.ok(predictionService.getLandmarks(city));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories(@RequestParam String city,
                                                       @RequestParam String landmark) {
        return ResponseEntity.ok(predictionService.getCategories(city, landmark));
    }
}
