package com.propridict.controller;

import com.propridict.dto.ContactDTO;
import com.propridict.dto.EMIRequest;
import com.propridict.dto.EMIResponse;
import com.propridict.model.ContactRequest;
import com.propridict.service.ContactService;
import com.propridict.service.EMIService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;
    private final EMIService emiService;

    @PostMapping("/contact")
    public ResponseEntity<Map<String, Object>> submitContact(@Valid @RequestBody ContactDTO dto) {
        ContactRequest saved = contactService.submit(dto);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Your consultation request has been submitted successfully!",
                "id", saved.getId()
        ));
    }

    @PostMapping("/emi/calculate")
    public ResponseEntity<EMIResponse> calculateEMI(@Valid @RequestBody EMIRequest request) {
        EMIResponse response = emiService.calculate(request);
        return ResponseEntity.ok(response);
    }
}
