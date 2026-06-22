package com.propridict.service;

import com.propridict.dto.PredictionRequest;
import com.propridict.dto.PredictionResponse;
import com.propridict.model.PropertyRate;
import com.propridict.repository.PropertyRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PredictionService {

    private static final double USD_RATE = 0.012;

    private final PropertyRateRepository propertyRateRepository;

    public List<String> getCities() {
        return propertyRateRepository.findDistinctCities();
    }

    public List<String> getLandmarks(String city) {
        return propertyRateRepository.findDistinctLandmarksByCity(city);
    }

    public List<String> getCategories(String city, String landmark) {
        return propertyRateRepository.findDistinctCategoriesByCityAndLandmark(city, landmark);
    }

    public PredictionResponse predict(PredictionRequest request) {
        Optional<PropertyRate> optRate = propertyRateRepository
                .findByCityAndLandmarkAndCategory(request.getCity(), request.getLandmark(), request.getCategory());

        if (optRate.isEmpty()) {
            throw new IllegalArgumentException("No data found for this city/landmark/category combination.");
        }

        PropertyRate rate = optRate.get();

        // Build time series
        int targetYear = request.getYear();
        List<Integer> years = new ArrayList<>();
        List<Double> baseRates = new ArrayList<>();

        for (int y = 2018; y <= targetYear; y++) {
            years.add(y);
            if (y <= 2024) {
                double t = (double) (y - 2018) / (2024 - 2018);
                baseRates.add(rate.getRate2018() + (rate.getRate2024() - rate.getRate2018()) * t);
            } else {
                baseRates.add(predictRate(y, rate.getRate2018(), rate.getRate2024()));
            }
        }

        double futureRate = predictRate(targetYear, rate.getRate2018(), rate.getRate2024());
        double cagr = computeCAGR(rate.getRate2018(), rate.getRate2024());

        // Premium factor
        double roadWidth = Math.min(Math.max(request.getRoadWidth(), 0), 999);
        double frontage = Math.min(Math.max(request.getFrontage(), 0), 999);
        double[] premium = computePremiumFactor(roadWidth, request.isCornerPlot(), frontage);
        double factor = premium[0];
        double premiumPct = premium[1];

        double adjustedRate = futureRate * factor;
        double totalPriceINR = adjustedRate * request.getSize();
        double totalPriceUSD = totalPriceINR * USD_RATE;

        // Build adjusted rates for chart
        List<Double> adjustedRates = new ArrayList<>();
        for (double br : baseRates) {
            adjustedRates.add(br * factor);
        }

        return PredictionResponse.builder()
                .baseRate(futureRate)
                .adjustedRate(adjustedRate)
                .totalPriceINR(totalPriceINR)
                .totalPriceUSD(totalPriceUSD)
                .premiumFactor(factor)
                .premiumPct(premiumPct)
                .cagr(cagr)
                .lat(rate.getLat())
                .lng(rate.getLng())
                .years(years)
                .baseRates(baseRates)
                .adjustedRates(adjustedRates)
                .build();
    }

    /**
     * Predict rate for a given year using CAGR extrapolation with decay factors.
     * Mirrors the JS predictRate() function.
     */
    private double predictRate(int year, double r2018, double r2024) {
        int span = 2024 - 2018;
        if (r2018 <= 0 || span <= 0) return r2024;

        double cagr = Math.pow(r2024 / r2018, 1.0 / span) - 1;
        double rate = r2024;

        for (int y = 2025; y <= year; y++) {
            double g = cagr;
            if (y > 2040) g *= 0.95;
            else if (y > 2035) g *= 0.98;
            rate *= (1 + g);
        }

        return Math.max(rate, 1000);
    }

    private double computeCAGR(double r2018, double r2024) {
        if (r2018 <= 0) return 0;
        return Math.pow(r2024 / r2018, 1.0 / 6.0) - 1;
    }

    /**
     * Compute premium factor based on road width, corner plot, and frontage.
     * Returns [factor, premiumPct].
     */
    private double[] computePremiumFactor(double roadFt, boolean isCorner, double frontageFt) {
        double p = 0;

        if (roadFt > 40) p += 0.06;
        else if (roadFt >= 31) p += 0.04;
        else if (roadFt >= 21) p += 0.02;

        if (frontageFt >= 50) p += 0.06;
        else if (frontageFt >= 40) p += 0.04;
        else if (frontageFt >= 30) p += 0.02;
        else if (frontageFt >= 20) p += 0.01;

        if (isCorner) p += 0.05;

        return new double[]{1 + p, p * 100};
    }
}
