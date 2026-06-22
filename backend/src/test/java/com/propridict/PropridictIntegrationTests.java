package com.propridict;

import com.propridict.dto.*;
import com.propridict.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PropridictIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PredictionService predictionService;

    @Autowired
    private EMIService emiService;

    @Autowired
    private StampDutyService stampDutyService;

    @Autowired
    private BankOfferService bankOfferService;

    @Autowired
    private ContactService contactService;

    // ===================================
    // Property Data Endpoints
    // ===================================

    @Test
    void getCities_shouldReturnCityList() throws Exception {
        mockMvc.perform(get("/api/properties/cities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThan(5)));
    }

    @Test
    void getCities_shouldIncludeKnownCities() {
        List<String> cities = predictionService.getCities();
        assertThat(cities).contains("Mumbai", "Delhi", "Bangalore", "Bhopal", "Indore");
    }

    @Test
    void getLandmarks_shouldReturnLandmarksForMumbai() throws Exception {
        mockMvc.perform(get("/api/properties/landmarks").param("city", "Mumbai"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThan(5)));
    }

    @Test
    void getCategories_shouldReturnCategories() throws Exception {
        mockMvc.perform(get("/api/properties/categories")
                        .param("city", "Bhopal")
                        .param("landmark", "MP Nagar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists());
    }

    // ===================================
    // Prediction Endpoint
    // ===================================

    @Test
    void predict_shouldReturnValidResponse() throws Exception {
        String body = """
                {
                    "city": "Bhopal",
                    "landmark": "MP Nagar",
                    "category": "Residential",
                    "size": 1500,
                    "year": 2028,
                    "roadWidth": 30,
                    "frontage": 35,
                    "cornerPlot": false
                }
                """;

        mockMvc.perform(post("/api/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.baseRate").isNumber())
                .andExpect(jsonPath("$.adjustedRate").isNumber())
                .andExpect(jsonPath("$.totalPriceINR").isNumber())
                .andExpect(jsonPath("$.totalPriceUSD").isNumber())
                .andExpect(jsonPath("$.premiumFactor").isNumber())
                .andExpect(jsonPath("$.cagr").isNumber())
                .andExpect(jsonPath("$.lat").isNumber())
                .andExpect(jsonPath("$.lng").isNumber())
                .andExpect(jsonPath("$.years").isArray())
                .andExpect(jsonPath("$.baseRates").isArray())
                .andExpect(jsonPath("$.adjustedRates").isArray());
    }

    @Test
    void predict_shouldReturn400ForMissingCity() throws Exception {
        String body = """
                {
                    "city": "",
                    "landmark": "MP Nagar",
                    "category": "Residential",
                    "size": 1500,
                    "year": 2028,
                    "roadWidth": 0,
                    "frontage": 0,
                    "cornerPlot": false
                }
                """;

        mockMvc.perform(post("/api/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void predict_shouldReturn400ForInvalidYear() throws Exception {
        String body = """
                {
                    "city": "Bhopal",
                    "landmark": "MP Nagar",
                    "category": "Residential",
                    "size": 1500,
                    "year": 2060,
                    "roadWidth": 0,
                    "frontage": 0,
                    "cornerPlot": false
                }
                """;

        mockMvc.perform(post("/api/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void predict_shouldReturn400ForInvalidCombination() throws Exception {
        String body = """
                {
                    "city": "Bhopal",
                    "landmark": "NonExistentArea",
                    "category": "Residential",
                    "size": 1500,
                    "year": 2028,
                    "roadWidth": 0,
                    "frontage": 0,
                    "cornerPlot": false
                }
                """;

        mockMvc.perform(post("/api/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ===================================
    // Service Layer Tests
    // ===================================

    @Test
    void predictionService_premiumFactors() {
        PredictionRequest request = PredictionRequest.builder()
                .city("Bhopal")
                .landmark("MP Nagar")
                .category("Residential")
                .size(1500)
                .year(2028)
                .roadWidth(50) // >40ft → +6%
                .frontage(60) // >=50ft → +6%
                .cornerPlot(true) // +5%
                .build();

        PredictionResponse response = predictionService.predict(request);

        // Premium factor should be 1 + 0.06 + 0.06 + 0.05 = 1.17
        assertThat(response.getPremiumFactor()).isCloseTo(1.17, within(0.001));
        assertThat(response.getPremiumPct()).isCloseTo(17.0, within(0.1));
        assertThat(response.getTotalPriceINR()).isGreaterThan(0);
        assertThat(response.getTotalPriceUSD()).isGreaterThan(0);
    }

    @Test
    void predictionService_chartSeriesHasCorrectLength() {
        PredictionRequest request = PredictionRequest.builder()
                .city("Mumbai")
                .landmark("Bandra West")
                .category("Residential")
                .size(1000)
                .year(2030)
                .roadWidth(0).frontage(0).cornerPlot(false)
                .build();

        PredictionResponse response = predictionService.predict(request);

        // 2018 to 2030 = 13 data points
        assertThat(response.getYears()).hasSize(13);
        assertThat(response.getBaseRates()).hasSize(13);
        assertThat(response.getAdjustedRates()).hasSize(13);
        assertThat(response.getYears().get(0)).isEqualTo(2018);
        assertThat(response.getYears().get(12)).isEqualTo(2030);
    }

    @Test
    void emiService_calculateCorrectly() {
        EMIRequest request = EMIRequest.builder()
                .totalPrice(10000000)
                .interestRate(8.5)
                .tenure(20)
                .downPaymentPct(20)
                .build();

        EMIResponse response = emiService.calculate(request);

        assertThat(response.getLoanPrincipal()).isCloseTo(8000000, within(1.0));
        assertThat(response.getDownPayment()).isCloseTo(2000000, within(1.0));
        assertThat(response.getMonthlyEMI()).isGreaterThan(60000); // rough sanity check
        assertThat(response.getTotalPayment()).isGreaterThan(response.getLoanPrincipal());
        assertThat(response.getTotalInterest()).isGreaterThan(0);
    }

    @Test
    void emiService_zeroRateReturnsZeroEMI() {
        double emi = emiService.calculateEMI(1000000, 0, 20);
        assertThat(emi).isEqualTo(0);
    }

    @Test
    void stampDutyService_getStatesReturnsAll() {
        List<String> states = stampDutyService.getStates();
        assertThat(states).hasSizeGreaterThan(10);
        assertThat(states).contains("Madhya Pradesh", "Delhi", "Maharashtra");
    }

    @Test
    void stampDutyService_calculateForMadhyaPradesh() {
        DutyRequest request = DutyRequest.builder()
                .state("Madhya Pradesh")
                .ownerType("Male")
                .propertyValue(5000000)
                .build();

        DutyResponse response = stampDutyService.calculate(request);

        // MP stamp base = 7.50%, registration = 1.00%
        assertThat(response.getStampDutyPct()).isCloseTo(7.5, within(0.01));
        assertThat(response.getRegistrationPct()).isCloseTo(1.0, within(0.01));
        assertThat(response.getStampDutyAmount()).isCloseTo(375000, within(1.0));
        assertThat(response.getRegistrationAmount()).isCloseTo(50000, within(1.0));
        assertThat(response.getTotalFees()).isCloseTo(425000, within(1.0));
        assertThat(response.getAllInCost()).isCloseTo(5425000, within(1.0));
    }

    @Test
    void stampDutyService_femaleDiscountInDelhi() {
        DutyRequest request = DutyRequest.builder()
                .state("Delhi")
                .ownerType("Female")
                .propertyValue(10000000)
                .build();

        DutyResponse response = stampDutyService.calculate(request);

        // Delhi female stamp = 4%, not 6%
        assertThat(response.getStampDutyPct()).isCloseTo(4.0, within(0.01));
    }

    @Test
    void bankOfferService_getHomeLoans() {
        List<BankOfferResponse> offers = bankOfferService.getOffers(
                "Home Loan", "All", "emi", 5000000, 20);

        assertThat(offers).hasSizeGreaterThan(5);
        // All should have computed fields
        for (BankOfferResponse offer : offers) {
            assertThat(offer.getEmiMin()).isGreaterThan(0);
            assertThat(offer.getProcessingFee()).isGreaterThan(0);
            assertThat(offer.getTenureUsed()).isGreaterThan(0);
        }
    }

    @Test
    void bankOfferService_sortByRate() {
        List<BankOfferResponse> offers = bankOfferService.getOffers(
                "Home Loan", "All", "rate", 5000000, 20);

        for (int i = 1; i < offers.size(); i++) {
            assertThat(offers.get(i).getRateMin())
                    .isGreaterThanOrEqualTo(offers.get(i - 1).getRateMin());
        }
    }

    @Test
    void bankOfferService_getLAPOffers() {
        List<BankOfferResponse> offers = bankOfferService.getOffers(
                "LAP", "All", "emi", 5000000, 15);

        assertThat(offers).hasSizeGreaterThan(2);
    }

    // ===================================
    // REST Endpoint Tests
    // ===================================

    @Test
    void bankOffersEndpoint_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/offers")
                        .param("type", "Home Loan")
                        .param("customer", "All")
                        .param("sort", "emi")
                        .param("loanAmount", "5000000")
                        .param("tenure", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThan(0)));
    }

    @Test
    void dutyStatesEndpoint_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/duty/states"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThan(10)));
    }

    @Test
    void dutyCalculateEndpoint_shouldReturn200() throws Exception {
        String body = """
                {
                    "state": "Maharashtra",
                    "ownerType": "Male",
                    "propertyValue": 10000000
                }
                """;

        mockMvc.perform(post("/api/duty/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("Maharashtra"))
                .andExpect(jsonPath("$.stampDutyPct").value(6.0))
                .andExpect(jsonPath("$.totalFees").isNumber());
    }

    @Test
    void contactEndpoint_shouldSubmitSuccessfully() throws Exception {
        String body = """
                {
                    "name": "Test User",
                    "email": "test@example.com",
                    "phone": "+91 12345 67890",
                    "purpose": "Property Buying",
                    "preferredDate": "2025-01-15",
                    "preferredTime": "10:00",
                    "message": "Integration test message"
                }
                """;

        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void contactEndpoint_shouldRejectMissingName() throws Exception {
        String body = """
                {
                    "name": "",
                    "email": "test@example.com",
                    "phone": "+91 12345 67890"
                }
                """;

        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void contactEndpoint_shouldRejectInvalidEmail() throws Exception {
        String body = """
                {
                    "name": "Test",
                    "email": "not-an-email",
                    "phone": "+91 12345 67890"
                }
                """;

        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void emiEndpoint_shouldCalculate() throws Exception {
        String body = """
                {
                    "totalPrice": 10000000,
                    "interestRate": 8.5,
                    "tenure": 20,
                    "downPaymentPct": 20
                }
                """;

        mockMvc.perform(post("/api/emi/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loanPrincipal").value(8000000.0))
                .andExpect(jsonPath("$.monthlyEMI").isNumber())
                .andExpect(jsonPath("$.totalInterest").isNumber());
    }
}
