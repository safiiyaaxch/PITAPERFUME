package com.scentify.service;

import com.scentify.model.Product;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Collectors;

/**
 * Service to integrate Google Gemini AI for intelligent perfume recommendations
 */
@Service
public class GoogleAIService {
    
    @Value("${google.ai.api-key}")
    private String apiKey;
    
    @Value("${google.ai.model:gemini-1.5-flash}")
    private String modelName;
    
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Get AI-powered recommendations for top 3 perfumes based on quiz answers
     * 
     * @param quizAnswers Quiz answers from customer
     * @param availableProducts List of available products from database
     * @return Top 3 recommended products based on AI analysis
     */
    public List<Product> getAIPoweredRecommendations(
            Map<String, String> quizAnswers, 
            List<Product> availableProducts) {
        try {
            if (availableProducts.isEmpty()) {
                System.out.println("⚠️ No available products for AI recommendation");
                return new ArrayList<>();
            }
            
            // Create a detailed product catalog for AI analysis
            String productCatalog = buildProductCatalog(availableProducts);
            
            // Build the prompt for AI
            String prompt = buildAIPrompt(quizAnswers, productCatalog);
            
            System.out.println("🤖 Sending request to Google Gemini AI...");
            
            // Call Gemini API
            String aiResponse = callGeminiAPI(prompt);
            
            // Parse AI response and extract product recommendations
            List<Product> recommendations = parseAIResponse(aiResponse, availableProducts);
            
            System.out.println("✅ AI generated " + recommendations.size() + " recommendations");
            
            return recommendations;
            
        } catch (Exception e) {
            System.out.println("❌ Error in AI-powered recommendation: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Build a detailed catalog of products for AI analysis
     */
    private String buildProductCatalog(List<Product> products) {
        StringBuilder catalog = new StringBuilder("Available Perfume Catalog:\n\n");
        
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            catalog.append(String.format(
                "%d. %s\n" +
                "   - Top Notes: %s\n" +
                "   - Middle Notes: %s\n" +
                "   - Base Notes: %s\n" +
                "   - Category: %s\n" +
                "   - Description: %s\n\n",
                i + 1,
                p.getProductName(),
                p.getTopNotes(),
                p.getBaseNotes(),
                p.getCategory(),
                truncateDescription(p.getDescription(), 100)
            ));
        }
        
        return catalog.toString();
    }
    
    /**
     * Build the AI prompt with customer preferences from 8-question quiz
     */
    private String buildAIPrompt(Map<String, String> answers, String productCatalog) {
        return String.format(
            "%s\n\n" +
            "Customer Quiz Preferences:\n" +
            "- Top Notes Preference: %s\n" +
            "- Middle Notes Preference: %s\n" +
            "- Base Notes Preference: %s\n" +
            "- Category Preference: %s\n" +
            "Task: Based on the customer quiz answers and the available perfume catalog, " +
            "recommend the TOP 3 most suitable perfumes. Match their preferences to product attributes.\n\n" +
            "Format your response EXACTLY as follows (very important):\n" +
            "1. [Perfume Name] - [Reason]\n" +
            "2. [Perfume Name] - [Reason]\n" +
            "3. [Perfume Name] - [Reason]",
            productCatalog,
            answers.getOrDefault("q1", "Unknown"),
            answers.getOrDefault("q2", "Unknown"),
            answers.getOrDefault("q3", "Unknown"),
            answers.getOrDefault("q4", "Unknown"),
            answers.getOrDefault("q5", "Unknown"),
            answers.getOrDefault("q6", "Unknown"),
            answers.getOrDefault("q7", "Unknown"),
            answers.getOrDefault("q8", "Unknown")
        );
    }
    
    /**
     * Call Google Gemini API and get the response
     */
    private String callGeminiAPI(String prompt) throws Exception {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new Exception("Google AI API key is not configured. Set GOOGLE_AI_API_KEY environment variable.");
        }
        
        System.out.println("✅ API Key configured (length: " + apiKey.length() + ")");
        
        String url = String.format(
            "%s%s:generateContent?key=%s",
            GEMINI_API_URL,
            modelName,
            apiKey
        );
        
        // Build request body
        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();
        
        Map<String, Object> content = new HashMap<>();
        List<Map<String, String>> parts = new ArrayList<>();
        
        Map<String, String> part = new HashMap<>();
        part.put("text", prompt);
        parts.add(part);
        
        content.put("parts", parts);
        contents.add(content);
        
        requestBody.put("contents", contents);
        
        // Add safety settings
        List<Map<String, String>> safetySettings = new ArrayList<>();
        String[] categories = {
            "HARM_CATEGORY_SEXUALLY_EXPLICIT",
            "HARM_CATEGORY_HATE_SPEECH",
            "HARM_CATEGORY_HARASSMENT",
            "HARM_CATEGORY_DANGEROUS_CONTENT"
        };
        
        for (String category : categories) {
            Map<String, String> setting = new HashMap<>();
            setting.put("category", category);
            setting.put("threshold", "BLOCK_NONE");
            safetySettings.add(setting);
        }
        requestBody.put("safetySettings", safetySettings);
        
        // Make HTTP request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        System.out.println("📡 Calling Gemini API: " + url.substring(0, url.indexOf("?key=")) + "?key=***");
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            if (response.getStatusCode() != HttpStatus.OK) {
                System.out.println("❌ API returned status: " + response.getStatusCode());
                System.out.println("Response body: " + response.getBody());
                throw new Exception("API returned status: " + response.getStatusCode());
            }
            
            // Parse response and extract text
            String responseBody = response.getBody();
            System.out.println("📨 AI Response (first 300 chars): " + responseBody.substring(0, Math.min(300, responseBody.length())));
            
            // Extract text content from JSON response
            return extractTextFromResponse(responseBody);
            
        } catch (Exception e) {
            System.out.println("❌ API call failed: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Extract text content from Gemini API JSON response
     */
    private String extractTextFromResponse(String jsonResponse) throws Exception {
        com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(jsonResponse);
        
        if (rootNode.has("candidates") && rootNode.get("candidates").isArray()) {
            var candidates = rootNode.get("candidates");
            if (candidates.size() > 0) {
                var firstCandidate = candidates.get(0);
                if (firstCandidate.has("content") && firstCandidate.get("content").has("parts")) {
                    var parts = firstCandidate.get("content").get("parts");
                    if (parts.isArray() && parts.size() > 0) {
                        var firstPart = parts.get(0);
                        if (firstPart.has("text")) {
                            return firstPart.get("text").asText();
                        }
                    }
                }
            }
        }
        
        throw new Exception("Could not extract text from API response");
    }
    
    /**
     * Parse AI response and extract recommended product names
     */
    private List<Product> parseAIResponse(String aiResponse, List<Product> availableProducts) {
        List<Product> recommendations = new ArrayList<>();
        
        try {
            System.out.println("🔍 AI Full Response:\n" + aiResponse);
            System.out.println("📊 Available products count: " + availableProducts.size());
            
            // Look for recommendation lines in the format "1. [Name]" or "RECOMMENDATION N: [Name]"
            String[] lines = aiResponse.split("\n");
            
            System.out.println("📝 Parsing " + lines.length + " lines...");
            
            for (String line : lines) {
                line = line.trim();
                System.out.println("  Checking line: " + line);
                
                // Match both "1. ProductName" and "RECOMMENDATION 1: ProductName" formats
                if (line.matches("^\\d+\\..*") || line.contains("RECOMMENDATION")) {
                    System.out.println("    ✅ Found recommendation format");
                    // Extract product name from the line
                    String productName = extractProductName(line, availableProducts);
                    if (productName != null) {
                        System.out.println("    ✅ Extracted product: " + productName);
                        // Find the product in the available list
                        Product product = availableProducts.stream()
                            .filter(p -> p.getProductName().equalsIgnoreCase(productName))
                            .findFirst()
                            .orElse(null);
                        
                        if (product != null && !recommendations.contains(product)) {
                            System.out.println("    ✅ Added to recommendations: " + product.getProductName());
                            recommendations.add(product);
                        }
                    }
                }
            }
            
            System.out.println("📌 Direct match count: " + recommendations.size());
            
            // If we couldn't extract exact names, fallback to fuzzy matching
            if (recommendations.isEmpty()) {
                System.out.println("🔍 No direct matches found, trying fuzzy matching...");
                recommendations = performFuzzyMatching(aiResponse, availableProducts);
                System.out.println("📌 Fuzzy match count: " + recommendations.size());
            }
            
            // Ensure we have at most 3 recommendations
            return recommendations.stream().limit(3).collect(Collectors.toList());
            
        } catch (Exception e) {
            System.out.println("⚠️ Error parsing AI response: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Extract product name from recommendation line
     */
    private String extractProductName(String line, List<Product> availableProducts) {
        // Try to find a product name mentioned in the line
        for (Product product : availableProducts) {
            if (line.contains(product.getProductName())) {
                return product.getProductName();
            }
        }
        
        // If no exact match, extract the text after the colon
        if (line.contains(":")) {
            String afterColon = line.substring(line.lastIndexOf(":") + 1).trim();
            
            // Try to find a matching product using partial matching
            for (Product product : availableProducts) {
                if (afterColon.toLowerCase().contains(product.getProductName().toLowerCase()) ||
                    product.getProductName().toLowerCase().contains(afterColon.toLowerCase())) {
                    return product.getProductName();
                }
            }
        }
        
        return null;
    }
    
    /**
     * Fallback: Perform fuzzy matching if exact name extraction fails
     */
    private List<Product> performFuzzyMatching(String aiResponse, List<Product> availableProducts) {
        System.out.println("🔍 Performing fuzzy matching on AI response...");
        
        // Simple scoring based on mentions in the response
        Map<Product, Integer> productScores = new HashMap<>();
        
        for (Product product : availableProducts) {
            int score = 0;
            String lowerResponse = aiResponse.toLowerCase();
            String lowerProductName = product.getProductName().toLowerCase();
            
            // Count occurrences
            int count = 0;
            int index = 0;
            while ((index = lowerResponse.indexOf(lowerProductName, index)) != -1) {
                count++;
                index += lowerProductName.length();
            }
            
            score += count * 10;
            
            // Check if mentioned in recommendation context
            if (lowerResponse.contains("recommendation") && 
                lowerResponse.substring(lowerResponse.indexOf("recommendation")).contains(lowerProductName)) {
                score += 50;
            }
            
            if (score > 0) {
                productScores.put(product, score);
            }
        }
        
        // Return top 3 by score
        return productScores.entrySet().stream()
            .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
            .limit(3)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    /**
     * Truncate description to a max length
     */
    private String truncateDescription(String description, int maxLength) {
        if (description == null) return "";
        if (description.length() <= maxLength) return description;
        return description.substring(0, maxLength) + "...";
    }
}
