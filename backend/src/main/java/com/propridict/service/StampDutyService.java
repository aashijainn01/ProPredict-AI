package com.propridict.service;

import com.propridict.dto.DutyRequest;
import com.propridict.dto.DutyResponse;
import com.propridict.model.StampDutyRule;
import com.propridict.repository.StampDutyRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StampDutyService {

    private final StampDutyRuleRepository stampDutyRuleRepository;

    public List<String> getStates() {
        return stampDutyRuleRepository.findAllStates();
    }

    public DutyResponse calculate(DutyRequest request) {
        StampDutyRule rule = stampDutyRuleRepository.findByState(request.getState())
                .orElseThrow(() -> new IllegalArgumentException("No stamp duty rules found for state: " + request.getState()));

        double stampPct = getStampRate(rule, request.getOwnerType());
        double regPct = rule.getRegistrationPct();
        double value = request.getPropertyValue();

        double stampAmount = value * (stampPct / 100.0);
        double regAmount = value * (regPct / 100.0);
        double totalFees = stampAmount + regAmount;

        return DutyResponse.builder()
                .state(request.getState())
                .ownerType(request.getOwnerType())
                .stampDutyPct(stampPct)
                .registrationPct(regPct)
                .stampDutyAmount(stampAmount)
                .registrationAmount(regAmount)
                .totalFees(totalFees)
                .allInCost(value + totalFees)
                .build();
    }

    /**
     * Get stamp duty rate based on owner type, falling back to base rate.
     */
    private double getStampRate(StampDutyRule rule, String ownerType) {
        if (ownerType == null) return rule.getStampBase();

        return switch (ownerType.toLowerCase()) {
            case "female" -> rule.getStampFemale() != null ? rule.getStampFemale() : rule.getStampBase();
            case "joint" -> rule.getStampJoint() != null ? rule.getStampJoint() : rule.getStampBase();
            default -> rule.getStampBase();
        };
    }
}
