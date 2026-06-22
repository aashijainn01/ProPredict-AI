package com.propridict.service;

import com.propridict.dto.BankOfferResponse;
import com.propridict.model.BankOffer;
import com.propridict.repository.BankOfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BankOfferService {

    private final BankOfferRepository bankOfferRepository;
    private final EMIService emiService;

    public List<BankOfferResponse> getOffers(String loanType, String customerType,
                                              String sortBy, double loanAmount, int tenureYears) {
        List<BankOffer> offers;

        if ("All".equalsIgnoreCase(customerType)) {
            offers = bankOfferRepository.findByLoanType(loanType);
        } else {
            offers = bankOfferRepository.findByLoanTypeAndCustomerIn(loanType,
                    List.of("All", customerType));
        }

        List<BankOfferResponse> result = offers.stream()
                .map(o -> toResponse(o, loanAmount, tenureYears))
                .collect(Collectors.toList());

        // Sort
        Comparator<BankOfferResponse> comparator = switch (sortBy != null ? sortBy.toLowerCase() : "emi") {
            case "rate" -> Comparator.comparingDouble(BankOfferResponse::getRateMin);
            case "fee" -> Comparator.comparingDouble(BankOfferResponse::getProcessingFee);
            default -> Comparator.comparingDouble(BankOfferResponse::getEmiMin);
        };

        result.sort(comparator);
        return result;
    }

    private BankOfferResponse toResponse(BankOffer offer, double loanAmount, int tenureYears) {
        int tenureUsed = Math.min(tenureYears, offer.getMaxTenure());
        double emiMin = emiService.calculateEMI(loanAmount, offer.getRateMin(), tenureUsed);
        double fee = computeFee(offer, loanAmount);

        return BankOfferResponse.builder()
                .bank(offer.getBank())
                .product(offer.getProduct())
                .customer(offer.getCustomer())
                .loanType(offer.getLoanType())
                .rateMin(offer.getRateMin())
                .rateMax(offer.getRateMax())
                .procPct(offer.getProcPct())
                .procMin(offer.getProcMin())
                .procMax(offer.getProcMax())
                .maxTenure(offer.getMaxTenure())
                .link(offer.getLink())
                .lastUpdated(offer.getLastUpdated())
                .tenureUsed(tenureUsed)
                .emiMin(emiMin)
                .processingFee(fee)
                .build();
    }

    private double computeFee(BankOffer offer, double loanAmount) {
        double pctAmount = (offer.getProcPct() / 100.0) * loanAmount;
        return Math.min(offer.getProcMax(), Math.max(offer.getProcMin(), pctAmount));
    }
}
