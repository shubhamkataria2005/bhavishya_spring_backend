package com.Shubham.carDealership.service;

import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@Service
public class AIRuleService {

    private List<String> allowedKeywords;
    private List<String> blockedKeywords;

    @PostConstruct
    public void init() {
        allowedKeywords = Arrays.asList(
                "oil", "mustard", "bhavishya", "kachi", "ghani", "price", "cost",
                "buy", "order", "purchase", "wholesale", "retail", "bulk",
                "distributor", "distribute", "health", "benefit", "vitamin",
                "cooking", "recipe", "pickle", "achar", "heart", "skin", "hair",
                "fssai", "iso", "certified", "grade", "pure", "natural",
                "500ml", "1 litre", "2 litre", "15 litre", "200ml",
                "sonipat", "haryana", "india", "delivery", "shipping",
                "hello", "hi", "thanks", "help", "what", "how", "where", "when"
        );

        blockedKeywords = Arrays.asList(
                "politics", "election", "president",
                "hack", "crack", "illegal", "steal",
                "sex", "porn", "gambling", "casino"
        );

        System.out.println("✅ Bhavishya Oil AI Rules initialized");
    }

    public boolean isQueryAllowed(String userMessage) {
        String lower = userMessage.toLowerCase();
        for (String blocked : blockedKeywords) {
            if (lower.contains(blocked)) return false;
        }
        return true;
    }

    public String getRefusalMessage() {
        return "I'm sorry, I can only help with questions about Bhavishya Kachi Ghani Mustard Oil. " +
                "Ask me about our products, prices, health benefits, or how to place an order! 🫒";
    }

    public String getCustomResponse(String userMessage) {
        String lower = userMessage.toLowerCase();

        if (lower.contains("hello") || lower.contains("hi") || lower.contains("hey")) {
            return "Namaste! 🙏 Welcome to Bhavishya Kachi Ghani Mustard Oil. How can I help you today? " +
                    "Ask me about our products, prices, health benefits, or distributor enquiries!";
        }

        if (lower.contains("contact") || lower.contains("phone") || lower.contains("call")) {
            return "You can reach us at:\n📞 +91-9653550600\n✉️ contact@bhavishyaoil.com\n" +
                    "📍 Sonipat, Haryana-131001, India";
        }

        if (lower.contains("address") || lower.contains("location") || lower.contains("where")) {
            return "We are located at:\nSurender Kala & Sons Private Limited\n" +
                    "Khasra No-11/20/2, Shahpur Turk, Sector-18\nSonipat, Haryana-131001, India";
        }

        if (lower.contains("certified") || lower.contains("iso") || lower.contains("fssai")) {
            return "Bhavishya Mustard Oil is:\n✅ ISO 22000:2018 Certified\n✅ ISO 9001:2015 Certified\n" +
                    "✅ FSSAI Licensed (No. 10019064002099)\n✅ Grade-I Quality\n✅ Trade Mark No. 3268585";
        }

        return null;
    }
}