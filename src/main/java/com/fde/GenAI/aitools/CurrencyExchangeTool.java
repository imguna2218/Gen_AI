package com.fde.GenAI.aitools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class CurrencyExchangeTool {

    @Value("${currency-exchange.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Tool(description = """
            Converts an amount from a base currency to a target currency.
            Also used to find the exchange rate between two currencies (by passing amount = 1.0).
            Only accepts valid ISO 4217 Three Letter Currency Codes.
            """)
    public double convertCurrency(
            @ToolParam(description = "The 3-letter base currency code (e.g., USD, EUR)")
            String baseCode,

            @ToolParam(description = "The 3-letter target currency code (e.g., GBP, JPY)")
            String targetCode,

            @ToolParam(description = "The amount to convert. Pass 1.0 if you only need the current exchange rate.")
            double amount) {

        System.out.println("CurrencyExchangeTool called for " + amount + " " + baseCode + " to " + targetCode);

        // Fail fast if the AI hallucinates bad currency codes
        if (baseCode == null || baseCode.length() != 3 || targetCode == null || targetCode.length() != 3) {
            throw new IllegalArgumentException("Invalid currency codes. Must be exactly 3 letters.");
        }

        String url = String.format("https://v6.exchangerate-api.com/v6/%s/pair/%s/%s/%f",
                apiKey, baseCode.toUpperCase(), targetCode.toUpperCase(), amount);

        try {
            // Using Map.class prevents the need for a dedicated DTO class for a simple JSON response
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null) {
                throw new RuntimeException("Empty response from ExchangeRate-API.");
            }

            if ("error".equals(response.get("result"))) {
                throw new RuntimeException("API Error: " + response.get("error-type"));
            }

            Object conversionResult = response.get("conversion_result");
            if (conversionResult instanceof Number) {
                return ((Number) conversionResult).doubleValue();
            }

            throw new RuntimeException("Missing or invalid 'conversion_result' in API response.");

        } catch (Exception e) {
            throw new RuntimeException("Failed to execute currency conversion: " + e.getMessage());
        }
    }
}