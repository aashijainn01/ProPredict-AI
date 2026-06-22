package com.propridict.controller;

import com.propridict.dto.BankOfferResponse;
import com.propridict.service.BankOfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
public class BankOfferController {

    private final BankOfferService bankOfferService;

    @GetMapping
    public ResponseEntity<List<BankOfferResponse>> getOffers(
            @RequestParam(defaultValue = "Home Loan") String type,
            @RequestParam(defaultValue = "All") String customer,
            @RequestParam(defaultValue = "emi") String sort,
            @RequestParam double loanAmount,
            @RequestParam(defaultValue = "20") int tenure) {
        List<BankOfferResponse> offers = bankOfferService.getOffers(type, customer, sort, loanAmount, tenure);
        return ResponseEntity.ok(offers);
    }
}
