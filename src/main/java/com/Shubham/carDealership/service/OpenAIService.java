package com.Shubham.carDealership.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.Shubham.carDealership.model.OilProduct;
import com.Shubham.carDealership.repository.OilProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@Service
public class OpenAIService {

    @Value("${openai.api.key:}")
    private String apiKey;

    @Autowired
    private AIRuleService aiRuleService;

    @Autowired
    private OilProductRepository productRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private boolean available = false;

    @PostConstruct
    public void init() {
        if (apiKey != null && !apiKey.isEmpty()) {
            available = true;
            System.out.println("✅ OpenAI Service initialized (GPT-4o-mini) for Bhavishya Oil");
        } else {
            System.out.println("⚠️ OpenAI API key not configured.");
        }
    }

    public String generateResponse(String userMessage) {
        // 1. Rule check
        if (!aiRuleService.isQueryAllowed(userMessage)) return aiRuleService.getRefusalMessage();

        // 2. Custom responses
        String custom = aiRuleService.getCustomResponse(userMessage);
        if (custom != null) return custom;

        // 3. GPT with live product context
        if (available) {
            String context = buildProductContext();
            String systemPrompt = "You are a helpful customer service assistant for Bhavishya Kachi Ghani Mustard Oil. " +
                    "A premium pure mustard oil brand by Surender Kala & Sons Pvt. Ltd., Sonipat, Haryana, India. " +
                    "Answer questions about products, prices, health benefits, cooking uses, certifications and distribution. " +
                    "Be friendly and professional. Keep answers under 150 words. Use Rs. for prices. " +
                    "If asked about prices, use the LIVE PRICES provided in context.";

            String userPrompt = "LIVE PRODUCT PRICES:\n" + context + "\n\nCustomer question: " + userMessage;

            String response = callChat(systemPrompt, userPrompt, 300, 0.7);
            if (response != null) return response;
        }

        return "Namaste! 🙏 I can help you with information about Bhavishya Kachi Ghani Mustard Oil. " +
                "Please call us at +91-9653550600 or email contact@bhavishyaoil.com for more details.";
    }

    private String buildProductContext() {
        try {
            List<OilProduct> products = productRepository.findAll();
            StringBuilder sb = new StringBuilder();
            for (OilProduct p : products) {
                sb.append(String.format("- %s (Size: %s, Weight: %s): Rs. %s | %s | %s\n",
                        p.getName(), p.getSize(), p.getWeight(),
                        p.getPrice().toPlainString(), p.getUsp(), p.getCategory().toUpperCase()));
            }
            return sb.toString();
        } catch (Exception e) {
            return "Products available: 200ml, 500ml, 1 Litre, 2 Litre, 15 Litre, 15 Kg Tin";
        }
    }

    private String callChat(String system, String user, int maxTokens, double temp) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", "gpt-4o-mini");
            body.put("max_tokens", maxTokens);
            body.put("temperature", temp);
            body.put("messages", List.of(
                    Map.of("role", "system", "content", system),
                    Map.of("role", "user", "content", user)
            ));
            String json = objectMapper.writeValueAsString(body);

            URL url = new URL("https://api.openai.com/v1/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes("utf-8"));
            }

            if (conn.getResponseCode() == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                Map<String, Object> result = objectMapper.readValue(sb.toString(), Map.class);
                List<Map<String, Object>> choices = (List<Map<String, Object>>) result.get("choices");
                return ((String) ((Map<String, Object>) choices.get(0).get("message")).get("content")).trim();
            }
        } catch (Exception e) {
            System.err.println("❌ OpenAI error: " + e.getMessage());
        }
        return null;
    }

    public boolean isAvailable() { return available; }
}