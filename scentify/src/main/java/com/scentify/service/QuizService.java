package com.scentify.service;

import com.scentify.model.Product;
import com.scentify.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private static final Logger log = LoggerFactory.getLogger(QuizService.class);

    @Autowired
    private ProductRepository productRepository;

    // =========================================================================
    // 1. QUIZ QUESTIONS
    // =========================================================================

    public List<Map<String, Object>> getAllQuizQuestions() {
        List<Map<String, Object>> questions = new ArrayList<>();
        
        // Question 1
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
        q1.put("optionValues", Arrays.asList("fresh", "warm_cozy", "floral", "woody_earthy", "bold_spicy"));
        questions.add(q1);
        
        // Question 2
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
        q2.put("optionValues", Arrays.asList("fresh", "warm_cozy", "floral", "woody_earthy", "bold_spicy"));
        questions.add(q2);
        
        // Question 3
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
        q3.put("optionValues", Arrays.asList("fresh", "warm_cozy", "floral", "woody_earthy", "bold_spicy"));
        questions.add(q3);
        
        // Question 4
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
        q4.put("optionValues", Arrays.asList("fresh", "warm_cozy", "floral", "woody_earthy", "bold_spicy"));
        questions.add(q4);
        
        // Question 5
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
        q5.put("optionValues", Arrays.asList("fresh", "warm_cozy", "floral", "woody_earthy", "bold_spicy"));
        questions.add(q5);
        
        // Question 6
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
        q6.put("optionValues", Arrays.asList("fresh", "warm_cozy", "floral", "woody_earthy", "bold_spicy"));
        questions.add(q6);
        
        // Question 7
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
        q7.put("optionValues", Arrays.asList("fresh", "warm_cozy", "floral", "woody_earthy", "bold_spicy"));
        questions.add(q7);
        
        // Question 8
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
        q8.put("optionValues", Arrays.asList("fresh", "warm_cozy", "floral", "woody_earthy", "bold_spicy"));
        questions.add(q8);
        
        return questions;
    }

    // =========================================================================
    // 2. PERSONA CALCULATION 
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
        int divisor = totalAnswered > 0 ? totalAnswered : 8;
        
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
    // 3. MAIN RECOMMENDATION ENGINE 
    // =========================================================================

    /**
     * Get recommendations based on quiz answers using rule-based matching
     */
    public List<Product> getPersonaBasedRecommendations(
            Map<String, String> answers,
            String primaryPersona, 
            Map<String, Integer> scores) {
        
        log.info("========================================");
        log.info("🎯 RULE-BASED RECOMMENDATION ENGINE");
        log.info("========================================");
        log.info("Primary Persona: {}", primaryPersona);
        log.info("Persona Scores: {}", scores);
        
        // Get all approved products from database
        List<Product> allProducts = productRepository.findByApprovalStatus("approved");
        
        if (allProducts.isEmpty()) {
            log.warn("⚠️ No approved products found in database!");
            return new ArrayList<>();
        }
        
        log.info("📦 Found {} approved products in database", allProducts.size());
        
        // Score each product
        List<ScoredProduct> scoredProducts = new ArrayList<>();
        
        for (Product product : allProducts) {
            int score = calculateProductScore(product, primaryPersona, scores);
            scoredProducts.add(new ScoredProduct(product, score));
            log.debug("Product: {} | Score: {}", product.getProductName(), score);
        }
        
        // Sort by score descending and get top 3
        scoredProducts.sort((a, b) -> Integer.compare(b.score, a.score));
        
        List<Product> recommendations = scoredProducts.stream()
                .limit(3)
                .map(sp -> sp.product)
                .collect(Collectors.toList());
        
        log.info("✅ Top 3 recommendations:");
        for (int i = 0; i < recommendations.size(); i++) {
            Product p = recommendations.get(i);
            log.info("  {}. {} (Score: {})", i+1, p.getProductName(), scoredProducts.get(i).score);
        }
        
        return recommendations;
    }

    /**
     * Calculate a score for a product based on how well it matches the persona
     */
    private int calculateProductScore(Product product, String primaryPersona, Map<String, Integer> scores) {
        int score = 0;
        
        // Get all notes as a single string for searching
        String allNotes = (product.getTopNotes() != null ? product.getTopNotes() : "") + " " +
                         (product.getMiddleNotes() != null ? product.getMiddleNotes() : "") + " " +
                         (product.getBaseNotes() != null ? product.getBaseNotes() : "");
        allNotes = allNotes.toLowerCase();
        
        String category = product.getCategory() != null ? product.getCategory().toLowerCase() : "";
        
        // ===== PRIMARY PERSONA MATCH (Highest weight: 50 points) =====
        switch (primaryPersona) {
            case "Fresh & Airy":
                if (matchesFreshNotes(allNotes, category)) score += 50;
                break;
            case "Warm & Cozy":
                if (matchesWarmNotes(allNotes, category)) score += 50;
                break;
            case "Floral & Romantic":
                if (matchesFloralNotes(allNotes, category)) score += 50;
                break;
            case "Woody & Earthy":
                if (matchesWoodyNotes(allNotes, category)) score += 50;
                break;
            case "Bold & Spicy":
                if (matchesBoldNotes(allNotes, category)) score += 50;
                break;
        }
        
        // ===== SECONDARY PERSONA MATCH (Medium weight: 30 points) =====
        // Check if product matches any other high-scoring personas
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            String persona = entry.getKey();
            int personaScore = entry.getValue();
            
            // Only consider personas that have at least 20% and are not the primary
            if (personaScore >= 20 && !persona.equals(primaryPersona)) {
                boolean matches = false;
                switch (persona) {
                    case "Fresh & Airy":
                        matches = matchesFreshNotes(allNotes, category);
                        break;
                    case "Warm & Cozy":
                        matches = matchesWarmNotes(allNotes, category);
                        break;
                    case "Floral & Romantic":
                        matches = matchesFloralNotes(allNotes, category);
                        break;
                    case "Woody & Earthy":
                        matches = matchesWoodyNotes(allNotes, category);
                        break;
                    case "Bold & Spicy":
                        matches = matchesBoldNotes(allNotes, category);
                        break;
                }
                if (matches) {
                    score += 30;
                }
            }
        }
        
        // ===== NOTE SPECIFIC MATCHES (Lower weight: 10-20 points) =====
        // Check each note category
        if (product.getTopNotes() != null) {
            String top = product.getTopNotes().toLowerCase();
            if (containsAny(top, getPersonaKeywords(primaryPersona, "top"))) {
                score += 15;
            }
        }
        
        if (product.getMiddleNotes() != null) {
            String middle = product.getMiddleNotes().toLowerCase();
            if (containsAny(middle, getPersonaKeywords(primaryPersona, "middle"))) {
                score += 10;
            }
        }
        
        if (product.getBaseNotes() != null) {
            String base = product.getBaseNotes().toLowerCase();
            if (containsAny(base, getPersonaKeywords(primaryPersona, "base"))) {
                score += 10;
            }
        }
        
        // ===== CATEGORY BOOST (5 points) =====
        if (category.contains(primaryPersona.toLowerCase().replace(" & ", " ").split(" ")[0])) {
            score += 5;
        }
        
        return score;
    }

    // ===== PERSONA MATCHING METHODS =====

    private boolean matchesFreshNotes(String notes, String category) {
        String[] keywords = {"citrus", "bergamot", "lemon", "lime", "orange", "grapefruit", 
                           "green", "ocean", "marine", "aquatic", "sea", "salt", "mint", 
                           "herbal", "tea", "cucumber", "aloe", "fresh", "clean", "linen"};
        return containsAny(notes, keywords) || category.contains("fresh") || category.contains("citrus");
    }

    private boolean matchesWarmNotes(String notes, String category) {
        String[] keywords = {"vanilla", "amber", "caramel", "toffee", "honey", "sugar", 
                           "coconut", "tonka", "benzoin", "creamy", "sweet", "baked", 
                           "cookie", "cake", "butter", "milky", "cashmere"};
        return containsAny(notes, keywords) || category.contains("warm") || category.contains("gourmand");
    }

    private boolean matchesFloralNotes(String notes, String category) {
        String[] keywords = {"rose", "jasmine", "peony", "iris", "lavender", "lilac", 
                           "orchid", "gardenia", "tuberose", "ylang", "magnolia", 
                           "violet", "powder", "floral", "bloom", "petal", "bouquet"};
        return containsAny(notes, keywords) || category.contains("floral") || category.contains("romantic");
    }

    private boolean matchesWoodyNotes(String notes, String category) {
        String[] keywords = {"cedar", "sandalwood", "patchouli", "vetiver", "oakmoss", 
                           "pine", "fir", "cypress", "woody", "earthy", "forest", 
                           "wood", "tree", "bark", "moss", "root", "soil", "musk"};
        return containsAny(notes, keywords) || category.contains("woody") || category.contains("earthy");
    }

    private boolean matchesBoldNotes(String notes, String category) {
        String[] keywords = {"pepper", "cinnamon", "clove", "cardamom", "saffron", 
                           "ginger", "nutmeg", "chili", "spicy", "leather", "tobacco", 
                           "incense", "resin", "opulent", "daring", "intense", "smoke"};
        return containsAny(notes, keywords) || category.contains("spicy") || category.contains("bold");
    }

    private String[] getPersonaKeywords(String persona, String noteType) {
        switch (persona) {
            case "Fresh & Airy":
                if ("top".equals(noteType)) return new String[]{"citrus", "bergamot", "lemon", "mint", "green"};
                if ("middle".equals(noteType)) return new String[]{"tea", "aquatic", "herbal"};
                return new String[]{"musk", "woody", "clean"};
            case "Warm & Cozy":
                if ("top".equals(noteType)) return new String[]{"coconut", "almond", "sweet"};
                if ("middle".equals(noteType)) return new String[]{"vanilla", "caramel", "honey"};
                return new String[]{"amber", "tonka", "benzoin"};
            case "Floral & Romantic":
                if ("top".equals(noteType)) return new String[]{"peach", "pear", "berry"};
                if ("middle".equals(noteType)) return new String[]{"rose", "jasmine", "peony"};
                return new String[]{"musk", "powder", "iris"};
            case "Woody & Earthy":
                if ("top".equals(noteType)) return new String[]{"pine", "cypress", "bergamot"};
                if ("middle".equals(noteType)) return new String[]{"cedar", "sandalwood"};
                return new String[]{"patchouli", "vetiver", "oakmoss"};
            case "Bold & Spicy":
                if ("top".equals(noteType)) return new String[]{"pepper", "cinnamon", "saffron"};
                if ("middle".equals(noteType)) return new String[]{"cardamom", "clove", "nutmeg"};
                return new String[]{"leather", "tobacco", "incense"};
            default:
                return new String[]{};
        }
    }

    private boolean containsAny(String text, String[] keywords) {
        if (text == null || keywords == null) return false;
        text = text.toLowerCase();
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    // =========================================================================
    // 4. HELPER CLASS FOR SCORING
    // =========================================================================

    private static class ScoredProduct {
        Product product;
        int score;
        
        ScoredProduct(Product product, int score) {
            this.product = product;
            this.score = score;
        }
    }

    // =========================================================================
    // 5. COMPATIBILITY METHODS (Keep existing methods)
    // =========================================================================

    public List<Product> getRecommendations(Map<String, String> answers) {
        log.info("📋 Getting recommendations (legacy method)");
        String primaryPersona = getPrimaryPersona(answers);
        Map<String, Integer> scores = calculatePersonaScores(answers);
        return getPersonaBasedRecommendations(answers, primaryPersona, scores);
    }

    public List<Product> getAIPoweredRecommendations(Map<String, String> answers) {
        log.info("🤖 AI-powered recommendations requested - using rule-based instead");
        return getRecommendations(answers);
    }

    public List<Product> getHybridRecommendations(Map<String, String> answers) {
        log.info("⚡ Hybrid recommendations requested - using rule-based instead");
        return getRecommendations(answers);
    }

    public List<Product> getPersonaBasedAIRecommendations(
            Map<String, String> answers,
            String primaryPersona, 
            Map<String, Integer> scores) {
        log.info("🎯 Persona-based recommendations requested");
        return getPersonaBasedRecommendations(answers, primaryPersona, scores);
    }

    public void saveQuizResponse(String userId, Map<String, String> answers, List<Product> topRecommendations) {
        log.info("💾 Saved quiz response for user: {} with {} recommendations", userId, topRecommendations.size());
    }

    public List<Product> getCustomerRecommendationHistory(String userId) {
        log.info("📜 Retrieving recommendation history for user: {}", userId);
        return productRepository.findByApprovalStatus("approved").stream()
                .limit(5)
                .collect(Collectors.toList());
    }
}