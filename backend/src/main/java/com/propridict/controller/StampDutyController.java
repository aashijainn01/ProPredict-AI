package com.propridict.controller;

import com.propridict.dto.DutyRequest;
import com.propridict.dto.DutyResponse;
import com.propridict.service.StampDutyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/duty")
@RequiredArgsConstructor
public class StampDutyController {

    private final StampDutyService stampDutyService;

    @GetMapping("/states")
    public ResponseEntity<List<String>> getStates() {
        return ResponseEntity.ok(stampDutyService.getStates());
    }

    @PostMapping("/calculate")
    public ResponseEntity<DutyResponse> calculate(@Valid @RequestBody DutyRequest request) {
        DutyResponse response = stampDutyService.calculate(request);
        return ResponseEntity.ok(response);
    }
}
