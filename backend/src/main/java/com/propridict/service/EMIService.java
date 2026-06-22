package com.propridict.service;

import com.propridict.dto.EMIRequest;
import com.propridict.dto.EMIResponse;
import org.springframework.stereotype.Service;

@Service
public class EMIService {

    /**
     * Calculate EMI using the standard formula:
     * EMI = P × r × (1+r)^n / ((1+r)^n - 1)
     * where P = principal, r = monthly rate, n = total months
     */
    public EMIResponse calculate(EMIRequest request) {
        double totalPrice = request.getTotalPrice();
        double downPct = Math.min(Math.max(request.getDownPaymentPct(), 0), 100);
        double rateAnnual = Math.min(Math.max(request.getInterestRate(), 0), 100);
        int tenureYears = Math.min(Math.max(request.getTenure(), 0), 100);

        double downPayment = totalPrice * (downPct / 100.0);
        double principal = Math.max(totalPrice - downPayment, 0);

        double emi = 0;
        double totalPayment = 0;
        double totalInterest = 0;

        if (rateAnnual > 0 && tenureYears > 0 && principal > 0) {
            double r = rateAnnual / 12.0 / 100.0;
            int n = tenureYears * 12;
            double pow = Math.pow(1 + r, n);
            emi = principal * r * pow / (pow - 1);
            totalPayment = emi * n;
            totalInterest = totalPayment - principal;
        }

        return EMIResponse.builder()
                .loanPrincipal(principal)
                .downPayment(downPayment)
                .monthlyEMI(emi)
                .totalInterest(totalInterest)
                .totalPayment(totalPayment)
                .build();
    }

    /**
     * Calculate EMI for a specific principal, rate, and tenure (used by BankOfferService).
     */
    public double calculateEMI(double principal, double rateAnnualPct, int tenureYears) {
        if (principal <= 0 || rateAnnualPct <= 0 || tenureYears <= 0) return 0;
        double r = rateAnnualPct / 12.0 / 100.0;
        int n = tenureYears * 12;
        double pow = Math.pow(1 + r, n);
        return principal * r * pow / (pow - 1);
    }
}
