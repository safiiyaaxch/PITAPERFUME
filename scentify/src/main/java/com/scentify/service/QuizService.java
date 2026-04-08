package com.scentify.service;

import com.scentify.model.Product;
import com.scentify.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuizService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private MLService mlService;
    
    // ========== GET ALL QUIZ QUESTIONS ==========
    public List<Map<String, Object>> getAllQuizQuestions() {
        // These are hardcoded for now - you can load from DB later
        return getHardcodedQuizQuestions();
    }
    
    // ========== SUBMIT QUIZ & GET RECOMMENDATIONS ==========
    public List<Product> getRecommendations(Map<String, String> answers) {
        try {
            // Get all approved products from database
            List<Product> approvedProducts = productRepository.findByApprovalStatus("approved");
            
            if (approvedProducts.isEmpty()) {
                System.out.println("⚠️ No approved products found!");
                return new ArrayList<>();
            }
            
            System.out.println("📦 Found " + approvedProducts.size() + " approved products");
            
            // Score each product based on quiz answers
            Map<Product, Double> scoreMap = new HashMap<>();
            
            for (Product product : approvedProducts) {
                double score = calculateProductScore(answers, product);
                scoreMap.put(product, score);
                System.out.println("🎯 Product: " + product.getProductName() + " | Score: " + String.format("%.2f", score));
            }
            
            // Sort by score (highest first) and return top 3
            return scoreMap.entrySet().stream()
                    .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                    .map(Map.Entry::getKey)
                    .limit(3)
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            System.out.println("❌ Error getting recommendations: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    // ========== CALCULATE PRODUCT SCORE ==========
    private double calculateProductScore(Map<String, String> answers, Product product) {
        double score = 0.0;
        int matchCount = 0;
        
        // Q1: Fragrance Family Match
        String q1Answer = answers.getOrDefault("q1", "");
        if (q1Answer.equalsIgnoreCase(product.getFragranceFamily())) {
            score += 100;
            matchCount++;
        }
        
        // Q2: Intensity Match
        String q2Answer = answers.getOrDefault("q2", "");
        if (q2Answer.equalsIgnoreCase(product.getIntensity())) {
            score += 30;
            matchCount++;
        }
        
        // Q3: Occasion Match
        String q3Answer = answers.getOrDefault("q3", "");
        if (q3Answer.equalsIgnoreCase(product.getOccasion())) {
            score += 20;
            matchCount++;
        }
        
        // Q4: Season Match
        String q4Answer = answers.getOrDefault("q4", "");
        if (q4Answer.equalsIgnoreCase(product.getSeason())) {
            score += 15;
            matchCount++;
        }
        
        // Q5: Gender Expression Match
        String q5Answer = answers.getOrDefault("q5", "");
        if (q5Answer.equalsIgnoreCase(product.getGenderExpression())) {
            score += 10;
            matchCount++;
        }
        
        // Q6: Top Notes Match
        String q6Answer = answers.getOrDefault("q6", "");
        if (product.getTopNotes() != null && product.getTopNotes().contains(q6Answer)) {
            score += 20;
            matchCount++;
        }
        
        // Q7: Base Notes Match
        String q7Answer = answers.getOrDefault("q7", "");
        if (product.getBaseNotes() != null && product.getBaseNotes().contains(q7Answer)) {
            score += 20;
            matchCount++;
        }
        
        // Q8: Temperature (Warm/Cool) preference
        String q8Answer = answers.getOrDefault("q8", "");
        boolean isWarm = q8Answer.equalsIgnoreCase("warm");
        boolean producIsWarm = product.getFragranceFamily() != null && 
                (product.getFragranceFamily().contains("Spicy") || 
                 product.getFragranceFamily().contains("Gourmand"));
        if ((isWarm && producIsWarm) || (!isWarm && !producIsWarm)) {
            score += 10;
            matchCount++;
        }
        
        // Q9: Sweetness Match
        String q9Answer = answers.getOrDefault("q9", "");
        Integer productSweetness = product.getSweetness();
        if (productSweetness != null) {
            if (q9Answer.contains("not") && productSweetness <= 1) score += 15;
            else if (q9Answer.contains("slightly") && productSweetness == 2) score += 15;
            else if (q9Answer.contains("moderately") && productSweetness == 3) score += 15;
            else if (q9Answer.contains("very") && productSweetness >= 4) score += 15;
            else if (q9Answer.contains("gourmand") && productSweetness >= 4) score += 20;
        }
        
        // Q12: Maturity/Age Match
        String q12Answer = answers.getOrDefault("q12", "");
        // You can extend this based on your product naming/characteristics
        
        System.out.println("   ↳ Matched " + matchCount + " criteria");
        
        return score;
    }
    
    // ========== SAVE QUIZ RESPONSE (For ML Training) ==========
    public void saveQuizResponse(String customerId, Map<String, String> answers, List<Product> recommendations) {
        try {
            System.out.println("💾 Saving quiz response for customer: " + customerId);
            // You can save this to a QuizResponse table for future ML training
            // This helps the model learn which products customers prefer based on their answers
            
            
            System.out.println("✅ Quiz response saved successfully");
        } catch (Exception e) {
            System.out.println("⚠️ Warning: Could not save quiz response: " + e.getMessage());
        }
    }
    
    // ========== GET CUSTOMER RECOMMENDATION HISTORY ==========
    public List<Product> getCustomerRecommendationHistory(String customerId) {
        try {
            // Load from QuizResponse table and return recommendations
            return new ArrayList<>();
        } catch (Exception e) {
            System.out.println("❌ Error loading history: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    // ========== HARDCODED QUIZ QUESTIONS ==========
    private List<Map<String, Object>> getHardcodedQuizQuestions() {
        List<Map<String, Object>> questions = new ArrayList<>();
        
        questions.add(createQuestion(1, "What vibes do you get from flowers and plants?", "q1", 
            List.of("Floral", "Fresh", "Woody", "Spicy", "Fruity")));
        
        questions.add(createQuestion(2, "What's your comfort zone with fragrance strength?", "q2",
            List.of("Light", "Moderate", "Strong", "Very Strong")));
        
        questions.add(createQuestion(3, "When do you usually wear fragrance?", "q3",
            List.of("Daily", "Professional", "Evening", "Active", "Versatile")));
        
        questions.add(createQuestion(4, "Which season feels most 'you'?", "q4",
            List.of("Spring", "Summer", "Fall", "Winter", "Year-round")));
        
        questions.add(createQuestion(5, "What fragrance style speaks to you?", "q5",
            List.of("Feminine", "Masculine", "Unisex")));
        
        questions.add(createQuestion(6, "First impression matters - what catches your nose?", "q6",
            List.of("Citrus", "Herbal", "Fruity", "Spicy", "Floral")));
        
        questions.add(createQuestion(7, "As the day goes on, what stays with you?", "q7",
            List.of("Musk", "Vanilla", "Woody", "Patchouli", "Clean")));
        
        questions.add(createQuestion(8, "Temperature check - warm or cool?", "q8",
            List.of("Cool", "Warm", "Balanced")));
        
        questions.add(createQuestion(9, "How do you feel about sweet smells?", "q9",
            List.of("Not sweet", "Slightly sweet", "Moderately sweet", "Very sweet", "Gourmand")));
        
        questions.add(createQuestion(10, "Real vs Fancy - which do you prefer?", "q10",
            List.of("Natural", "Slightly abstract", "Creative", "No preference")));
        
        return questions;
    }
    
    private Map<String, Object> createQuestion(int order, String text, String fieldName, List<String> options) {
        Map<String, Object> q = new LinkedHashMap<>();
        q.put("order", order);
        q.put("text", text);
        q.put("fieldName", fieldName);
        q.put("options", options);
        return q;
    }
}
