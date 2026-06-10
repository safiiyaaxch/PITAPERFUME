package com.scentify.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scentify.model.Product;
import com.scentify.repository.ProductRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private static final Logger log = LoggerFactory.getLogger(QuizService.class);

    @Value("${google.ai.api-key}")
    private String apiKey;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public QuizService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    // =========================================================================
    // 1. QUIZ QUESTIONS
    // =========================================================================

    public List<Map<String, Object>> getAllQuizQuestions() {
        List<Map<String, Object>> questions = new ArrayList<>();
        
        Map<String, Object> q1 = new HashMap<>();
        q1.put("id", "q1");
        q1.put("text", "What is your dream way to spend a slow morning?");
        q1.put("description", "Choose the atmosphere that feels most soothing to you.");
        q1.put("options", Arrays.asList(
            "Opening the windows to let a crisp, cool breeze flow through the room",
            "Sipping a warm vanilla latte tucked under an incredibly soft knit blanket",
            "Walking through a gentle garden filled with blooming, dew-kissed flowers",
            "Sitting in a quiet, wooden library corner with a book in your hands",
            "Brewing a comforting pot of rich spiced tea with cinnamon and clove"
        ));
        questions.add(q1);
        
        Map<String, Object> q2 = new HashMap<>();
        q2.put("id", "q2");
        q2.put("text", "Which of these refreshing drinks do you order most often?");
        q2.put("description", "Your flavor preferences heavily translate to your fragrance taste.");
        q2.put("options", Arrays.asList(
            "Iced cucumber water, cold green tea, or fresh sparkling lemonade",
            "Sweet hot cocoa, creamy caramel macchiato, or vanilla tea latte",
            "A glass of fresh peach nectar, or a light rose-infused milk drink",
            "Strong black coffee, smoky dark tea, or a rich earthy matcha",
            "A warm spiced apple cider, mulled berry juice, or espresso with cinnamon"
        ));
        questions.add(q2);
        
        Map<String, Object> q3 = new HashMap<>();
        q3.put("id", "q3");
        q3.put("text", "Which scent from everyday life brings you instant comfort?");
        q3.put("description", "Select the one that sparks absolute nostalgia or peace.");
        q3.put("options", Arrays.asList(
            "Clean linen fresh out of the dryer or the salty spray of sea air",
            "Warm sugar cookies baking or toasted caramel drizzled on ice cream",
            "A bouquet of freshly cut roses and jasmines on a sunny dining table",
            "The earthy scent of fireplace logs, cedar wood chips, or a pine forest hike",
            "Warm spices in a boutique kitchen, or the mysterious scent of burning incense"
        ));
        questions.add(q3);
        
        Map<String, Object> q4 = new HashMap<>();
        q4.put("id", "q4");
        q4.put("text", "Which fabric or clothing style makes you feel most like yourself?");
        q4.put("description", "Scent is the invisible clothing you wear daily.");
        q4.put("options", Arrays.asList(
            "Light, airy white linen or a simple, crisp cotton t-shirt",
            "A fluffy cashmere scarf or a warm, oversized knit sweater",
            "A beautiful silk dress, delicate lace top, or romantic flowy patterns",
            "A structured wool coat, earthy suede jacket, or classic leather boots",
            "A bold satin blazer, glamorous dark velvet, or a statement accessory"
        ));
        questions.add(q4);
        
        Map<String, Object> q5 = new HashMap<>();
        q5.put("id", "q5");
        q5.put("text", "If you were given a free afternoon just to relax, where would you go?");
        q5.put("description", "Choose the environment that feels like the ultimate escape.");
        q5.put("options", Arrays.asList(
            "A breezy coastal boardwalk with wave mist and coastal breeze",
            "An artisan bakery filled with sweet vanilla tarts and warm butter aromas",
            "A lush, colorful glass greenhouse garden with jasmine and orchid vines",
            "A cozy cabin deep in redwood territory with campfires and cedarwood trees",
            "A dimly lit, gorgeous speakeasy lounge with velvet couches and spice aromas"
        ));
        questions.add(q5);
        
        Map<String, Object> q6 = new HashMap<>();
        q6.put("id", "q6");
        q6.put("text", "What kind of evening sweet treat or dessert do you naturally prefer?");
        q6.put("description", "Taste buds and scent descriptors share an intimate connection.");
        q6.put("options", Arrays.asList(
            "A light, refreshing lemon sorbet or coconut water ice pops",
            "A rich vanilla bean crème brûlée or warm chocolate chip cookies",
            "A delicate lavender macaroon or a fresh strawberry tartlet",
            "A simple piece of premium dark chocolate with roasted timber hazelnuts",
            "A slice of spiced gingerbread with cinnamon ice cream or baked honey pears"
        ));
        questions.add(q6);
        
        Map<String, Object> q7 = new HashMap<>();
        q7.put("id", "q7");
        q7.put("text", "How would you love your ideal fragrance to make you (and others) feel?");
        q7.put("description", "Determine the subtle aura you want to project.");
        q7.put("options", Arrays.asList(
            "Effortlessly fresh, revitalized, and bright—like I just emerged from a peaceful shower",
            "Sweet, incredibly comforting, and gentle—like an inviting, warm hug",
            "Elegant, lovely, and highly refined—like walking with a beautiful floral bouquet",
            "Calm, mysterious, and naturally grounded—like a reassuring and wise silent partner",
            "Bold, completely captivating, and unique—making a beautiful, unforgettable statement"
        ));
        questions.add(q7);
        
        Map<String, Object> q8 = new HashMap<>();
        q8.put("id", "q8");
        q8.put("text", "What kind of natural light or ambient environment speaks to your soul?");
        q8.put("description", "Let's capture the overall atmospheric mood of your personality.");
        q8.put("options", Arrays.asList(
            "Crisp, sparkling blue morning light that fills the sky with energy",
            "Glittering golden hour light casting a rich amber glow over the room",
            "A soft, pastel-colored sunrise with sweet shades of lavender and peach",
            "A quiet, misty fog rolling over deep mountains with giant pine shadows",
            "A warm, candle-lit room under a deep night sky full of stars"
        ));
        questions.add(q8);
        
        return questions;
    }

    // =========================================================================
    // 2. PERSONA SPECTRUM CALCULATORS
    // =========================================================================

    public Map<String, Integer> calculatePersonaScores(Map<String, String> answers) {
        Map<String, Integer> counts = new HashMap<>();
        
        counts.put("Fresh & Airy", 0);
        counts.put("Warm & Cozy", 0);
        counts.put("Floral & Romantic", 0);
        counts.put("Woody & Earthy", 0);
        counts.put("Bold & Spicy", 0);

        int totalAnswered = 0;

        for (int i = 1; i <= 8; i++) {
            String choice = answers.get("q" + i);
            if (choice != null && !choice.trim().isEmpty()) {
                totalAnswered++;
                switch (choice.toLowerCase().trim()) {
                    case "fresh":
                        counts.put("Fresh & Airy", counts.get("Fresh & Airy") + 1);
                        break;
                    case "warm_cozy":
                        counts.put("Warm & Cozy", counts.get("Warm & Cozy") + 1);
                        break;
                    case "floral":
                        counts.put("Floral & Romantic", counts.get("Floral & Romantic") + 1);
                        break;
                    case "woody_earthy":
                        counts.put("Woody & Earthy", counts.get("Woody & Earthy") + 1);
                        break;
                    case "bold_spicy":
                        counts.put("Bold & Spicy", counts.get("Bold & Spicy") + 1);
                        break;
                }
            }
        }

        Map<String, Integer> finalPercentages = new HashMap<>();
        final int divisor = totalAnswered > 0 ? totalAnswered : 8;
        
        counts.forEach((key, val) -> {
            int percentage = (int) Math.round((val.doubleValue() / divisor) * 100);
            finalPercentages.put(key, percentage);
        });

        return finalPercentages;
    }

    public String getPrimaryPersona(Map<String, String> answers) {
        Map<String, Integer> scores = calculatePersonaScores(answers);
        return scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Fresh & Airy");
    }

    // =========================================================================
    // 3. FALLBACK RECOMMENDATION METHODS
    // =========================================================================

    public List<Product> getRecommendations(Map<String, String> answers) {
        log.info("📋 USING FALLBACK RECOMMENDATIONS (Rule-based)");
        List<Product> allProducts = getAllAvailableProductsFromDb();
        if (allProducts.isEmpty()) {
            log.warn("No products found in database for fallback");
            return Collections.emptyList();
        }
        
        String primaryPersona = getPrimaryPersona(answers);
        log.info("Fallback using primary persona: {}", primaryPersona);
        
        List<Product> filtered = allProducts.stream()
                .filter(p -> matchesPersona(p, primaryPersona))
                .limit(3)
                .collect(Collectors.toList());
        
        log.info("Fallback returned {} products", filtered.size());
        return filtered;
    }
    
    private boolean matchesPersona(Product product, String persona) {
        String notes = (product.getTopNotes() + " " + 
                       product.getMiddleNotes() + " " + 
                       product.getBaseNotes()).toLowerCase();
        
        switch(persona) {
            case "Fresh & Airy":
                return notes.contains("citrus") || notes.contains("aquatic") || 
                       notes.contains("marine") || notes.contains("bergamot") || 
                       notes.contains("lemon") || notes.contains("ocean");
            case "Warm & Cozy":
                return notes.contains("vanilla") || notes.contains("amber") || 
                       notes.contains("caramel") || notes.contains("tonka");
            case "Floral & Romantic":
                return notes.contains("rose") || notes.contains("jasmine") || 
                       notes.contains("peony") || notes.contains("floral");
            case "Woody & Earthy":
                return notes.contains("sandalwood") || notes.contains("cedar") || 
                       notes.contains("vetiver") || notes.contains("patchouli");
            case "Bold & Spicy":
                return notes.contains("pepper") || notes.contains("cinnamon") || 
                       notes.contains("saffron") || notes.contains("leather");
            default:
                return true;
        }
    }

    public List<Product> getAIPoweredRecommendations(Map<String, String> answers) {
        log.info("🤖 AI-Powered Recommendations requested");
        Map<String, Integer> scores = calculatePersonaScores(answers);
        String primaryPersona = getPrimaryPersona(answers);
        return getPersonaBasedAIRecommendations(answers, primaryPersona, scores);
    }

    public List<Product> getHybridRecommendations(Map<String, String> answers) {
        log.info("⚡ Hybrid Recommendations requested");
        try {
            List<Product> aiRecs = getAIPoweredRecommendations(answers);
            if (aiRecs != null && !aiRecs.isEmpty()) {
                log.info("✅ Hybrid using AI recommendations: {}", aiRecs.size());
                return aiRecs;
            }
        } catch (Exception e) {
            log.warn("⚠️ AI failed in hybrid mode: {}", e.getMessage());
        }
        log.info("🔄 Hybrid falling back to traditional recommendations");
        return getRecommendations(answers);
    }

    public void saveQuizResponse(String userId, Map<String, String> answers, List<Product> topRecommendations) {
        log.info("💾 Saved quiz response for user: {} with {} recommendations", userId, topRecommendations.size());
    }

    public List<Product> getCustomerRecommendationHistory(String userId) {
        log.info("📜 Retrieving recommendation history for user: {}", userId);
        return getAllAvailableProductsFromDb();
    }

    // =========================================================================
    // 4. MAIN AI RECOMMENDATION ENGINE
    // =========================================================================

    public List<Product> getPersonaBasedAIRecommendations(
            Map<String, String> allAnswers,
            String primaryPersona, 
            Map<String, Integer> scores) {
        
        log.info("╔══════════════════════════════════════════════════════════════╗");
        log.info("║         🚨 AI RECOMMENDATION METHOD CALLED 🚨                ║");
        log.info("╚══════════════════════════════════════════════════════════════╝");
        log.info("📅 TIMESTAMP: {}", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        log.info("🎯 PRIMARY PERSONA: {}", primaryPersona);
        log.info("📊 PERSONA SCORES: {}", scores);
        
        // Check API Key
        boolean hasApiKey = apiKey != null && !apiKey.isEmpty() && !apiKey.equals("YOUR_ACTUAL_GEMINI_API_KEY");
        log.info("🔑 API Key configured: {}", hasApiKey);
        
        if (!hasApiKey) {
            log.error("❌ No valid API key found! Using fallback.");
            return getRecommendations(allAnswers).stream().limit(3).collect(Collectors.toList());
        }
        
        // Get Products
        List<Product> availableProducts = getAllAvailableProductsFromDb();
        if (availableProducts.isEmpty()) {
            log.error("❌ No products in database!");
            return Collections.emptyList();
        }
        
        log.info("📦 Found {} products in database", availableProducts.size());

        try {
            log.info("📡 Attempting Gemini API call...");
            
            String prompt = buildPrompt(allAnswers, primaryPersona, scores, availableProducts);
            String aiResponse = callGeminiAPI(prompt);
            
            log.info("✅ Gemini API call successful!");
            log.info("📝 Response: {}", aiResponse.length() > 500 ? aiResponse.substring(0, 500) + "..." : aiResponse);
            
            // Parse the response
            List<String> recommendedProductIds = parseAIResponse(aiResponse);
            log.info("🎯 AI recommended product IDs: {}", recommendedProductIds);
            
            // Match with database
            List<Product> recommendations = availableProducts.stream()
                    .filter(p -> recommendedProductIds.contains(p.getProductId()))
                    .limit(3)
                    .collect(Collectors.toList());
            
            if (recommendations.isEmpty()) {
                log.warn("⚠️ No matches found for AI recommendations, using fallback");
                return getRecommendations(allAnswers).stream().limit(3).collect(Collectors.toList());
            }
            
            log.info("✅ Final AI recommendations: {}", recommendations.stream().map(Product::getProductName).collect(Collectors.toList()));
            return recommendations;
            
        } catch (Exception e) {
            log.error("❌ AI recommendation failed: {}", e.getMessage());
            log.info("🔄 Falling back to traditional recommendations");
            return getRecommendations(allAnswers).stream().limit(3).collect(Collectors.toList());
        }
    }
    
    private String buildPrompt(Map<String, String> answers, String primaryPersona, 
                                Map<String, Integer> scores, List<Product> products) {
        
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a perfume recommendation expert. Based on the user's quiz answers, recommend 3 perfumes from the list below.\n\n");
        
        prompt.append("USER'S QUIZ ANSWERS:\n");
        for (int i = 1; i <= 8; i++) {
            String answer = answers.get("q" + i);
            if (answer != null) {
                prompt.append("Q").append(i).append(": ").append(getAnswerDescription(i, answer)).append("\n");
            }
        }
        
        prompt.append("\nPERSONA: ").append(primaryPersona).append("\n");
        prompt.append("PERSONA BREAKDOWN: ").append(scores).append("\n\n");
        
        prompt.append("AVAILABLE PERFUMES (ONLY pick from these):\n");
        for (Product p : products) {
            prompt.append("ID: ").append(p.getProductId())
                  .append(" | Name: ").append(p.getProductName())
                  .append(" | Category: ").append(p.getCategory())
                  .append(" | Top Notes: ").append(p.getTopNotes())
                  .append(" | Middle Notes: ").append(p.getMiddleNotes())
                  .append(" | Base Notes: ").append(p.getBaseNotes())
                  .append("\n");
        }
        
        prompt.append("\nIMPORTANT: Return ONLY a JSON object with this exact format:\n");
        prompt.append("{\"recommendations\": [\"PRODUCT_ID_1\", \"PRODUCT_ID_2\", \"PRODUCT_ID_3\"]}\n");
        prompt.append("Do not include any other text or explanation.\n");
        
        return prompt.toString();
    }
    
    private String getAnswerDescription(int questionNum, String answer) {
        switch(answer) {
            case "fresh":
                String[] freshDesc = {
                    "Enjoys crisp, cool breezes and fresh morning air",
                    "Prefers light, refreshing drinks like cucumber water or green tea",
                    "Comforted by clean linen and sea air scents",
                    "Likes light, airy fabrics like linen and cotton",
                    "Drawn to coastal breezes and ocean mist",
                    "Enjoys light, refreshing desserts like lemon sorbet",
                    "Wants to feel revitalized, fresh, and bright",
                    "Attracted to sparkling blue morning light"
                };
                return freshDesc[questionNum - 1];
            case "warm_cozy":
                String[] warmDesc = {
                    "Enjoys warm vanilla latte moments under cozy blankets",
                    "Prefers sweet, indulgent drinks like hot cocoa or vanilla latte",
                    "Comforted by warm sugar cookies and caramel scents",
                    "Likes soft, warm fabrics like cashmere and knits",
                    "Drawn to artisan bakeries with warm butter aromas",
                    "Enjoys rich desserts like crème brûlée or chocolate chip cookies",
                    "Wants to feel comforting, sweet, and gentle",
                    "Attracted to warm golden hour amber glow"
                };
                return warmDesc[questionNum - 1];
            case "floral":
                String[] floralDesc = {
                    "Enjoys walking through blooming gardens with dew-kissed flowers",
                    "Prefers delicate drinks like peach nectar or rose milk",
                    "Comforted by fresh roses and jasmine scents",
                    "Likes elegant fabrics like silk and lace",
                    "Drawn to lush greenhouse gardens with orchids",
                    "Enjoys delicate desserts like lavender macarons",
                    "Wants to feel elegant, refined, and lovely",
                    "Attracted to soft pastel sunrises"
                };
                return floralDesc[questionNum - 1];
            case "woody_earthy":
                String[] woodyDesc = {
                    "Enjoys quiet wooden library corners with books",
                    "Prefers strong, earthy drinks like black coffee or matcha",
                    "Comforted by fireplace logs and cedarwood scents",
                    "Likes structured, grounded fabrics like wool and leather",
                    "Drawn to cozy cabins in redwood forests",
                    "Enjoys dark chocolate with roasted hazelnuts",
                    "Wants to feel calm, mysterious, and grounded",
                    "Attracted to misty pine forests and mountains"
                };
                return woodyDesc[questionNum - 1];
            case "bold_spicy":
                String[] boldDesc = {
                    "Enjoys rich spiced tea with cinnamon and clove",
                    "Prefers warm spiced drinks like mulled cider",
                    "Comforted by warm spices and incense",
                    "Likes bold fabrics like velvet and satin",
                    "Drawn to mysterious speakeasy lounges",
                    "Enjoys spiced desserts like gingerbread",
                    "Wants to feel bold, captivating, and unique",
                    "Attracted to candlelit rooms under starry skies"
                };
                return boldDesc[questionNum - 1];
            default:
                return "No preference specified";
        }
    }
    
    private String callGeminiAPI(String prompt) throws Exception {
        // IMPORTANT: Use v1beta API with gemini-pro model
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + apiKey;
        
        log.info("🌐 Calling Gemini API at: {}", url.replace(apiKey, "***"));
        
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
        
        // Add generation config
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.2);
        generationConfig.put("topK", 40);
        generationConfig.put("topP", 0.95);
        requestBody.put("generationConfig", generationConfig);
        
        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("❌ API returned status: {}", response.getStatusCode());
                log.error("Response body: {}", response.getBody());
                throw new Exception("API returned " + response.getStatusCode());
            }
            
            String responseBody = response.getBody();
            log.debug("Raw API response: {}", responseBody);
            
            // Parse the response
            var rootNode = objectMapper.readTree(responseBody);
            String textContent = rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
            
            log.info("✅ Extracted text: {}", textContent);
            return textContent;
            
        } catch (Exception e) {
            log.error("❌ API call failed: {}", e.getMessage());
            throw new Exception("Gemini API call failed: " + e.getMessage(), e);
        }
    }
    
    private List<String> parseAIResponse(String aiResponse) {
        List<String> productIds = new ArrayList<>();
        
        try {
            // Try to parse as JSON
            var rootNode = objectMapper.readTree(aiResponse);
            if (rootNode.has("recommendations")) {
                var recs = rootNode.get("recommendations");
                if (recs.isArray()) {
                    for (var rec : recs) {
                        if (rec.isTextual()) {
                            productIds.add(rec.asText());
                        } else if (rec.has("productId")) {
                            productIds.add(rec.get("productId").asText());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse JSON, trying text extraction: {}", e.getMessage());
            
            // Fallback: Extract product IDs from text
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("P\\d+");
            java.util.regex.Matcher matcher = pattern.matcher(aiResponse);
            while (matcher.find()) {
                String id = matcher.group();
                if (!productIds.contains(id)) {
                    productIds.add(id);
                }
            }
        }
        
        // Limit to 3 recommendations
        return productIds.stream().limit(3).collect(Collectors.toList());
    }

    @Autowired
    private ProductRepository productRepository;

    private List<Product> getAllAvailableProductsFromDb() {
        try {
            log.info("🔍 Querying database for approved products...");
            List<Product> products = productRepository.findByApprovalStatus("approved");
            log.info("📊 Database returned {} products", products.size());
            return products;
        } catch (Exception e) {
            log.error("❌ Database query failed: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}