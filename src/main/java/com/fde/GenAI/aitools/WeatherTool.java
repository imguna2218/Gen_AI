package com.fde.GenAI.aitools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class WeatherTool {

    @Value("${weather-api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Tool(description = "Fetches the real-time current weather for a specified city.")
    public String getWeather(
            @ToolParam(description = "The exact name of the city (e.g., London, Tokyo, Vijayawada)")
            String city) {

        System.out.println("WeatherTool called for city: " + city);

        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("City name cannot be empty.");
        }

        // Weatherstack uses access_key and query
        String url = String.format("https://api.weatherstack.com/current?access_key=%s&query=%s",
                apiKey, city.trim());

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null) {
                throw new RuntimeException("Received empty response from Weatherstack API.");
            }

            // Weatherstack returns an 'error' object if the request fails (e.g., bad API key)
            if (response.containsKey("error")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> error = (Map<String, Object>) response.get("error");
                throw new RuntimeException("API Error: " + error.get("info"));
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> current = (Map<String, Object>) response.get("current");
            @SuppressWarnings("unchecked")
            Map<String, Object> location = (Map<String, Object>) response.get("location");

            if (current == null || location == null) {
                throw new RuntimeException("Invalid response structure from Weatherstack.");
            }

            String locName = (String) location.get("name");
            Object temperature = current.get("temperature");

            @SuppressWarnings("unchecked")
            List<String> descriptions = (List<String>) current.get("weather_descriptions");
            String weatherDesc = (descriptions != null && !descriptions.isEmpty()) ? descriptions.get(0) : "Unknown";

            // Return a clean, human-readable string for the LLM to easily parse
            return String.format("Current weather in %s: %s°C, %s.", locName, temperature, weatherDesc);

        } catch (Exception e) {
            throw new RuntimeException("Failed to execute weather fetch: " + e.getMessage());
        }
    }
}