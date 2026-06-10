package com.scentify.controller;

import com.scentify.model.User;
import com.scentify.model.Product;
import com.scentify.service.QuizService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/customer/quiz")
public class QuizController {

    @Autowired
    private QuizService quizService;

    // ========== SESSION VALIDATION ==========
    private boolean isCustomerLoggedIn(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        return user != null && "customer".equalsIgnoreCase(user.getRole());
    }

    private User getLoggedInUser(HttpSession session) {
        return (User) session.getAttribute("loggedInUser");
    }

    // ========== SHOW QUIZ START PAGE ==========
    @GetMapping("/start")
    public String startQuiz(HttpSession session, Model model) {
        if (!isCustomerLoggedIn(session)) {
            return "redirect:/login";
        }
        User user = getLoggedInUser(session);
        model.addAttribute("user", user);
        return "customer/manage-preference/quiz-start";
    }

    // ========== GET ALL QUIZ QUESTIONS ==========
    @GetMapping("/questions")
    public String getQuizQuestions(HttpSession session, Model model) {
        if (!isCustomerLoggedIn(session)) {
            return "redirect:/login";
        }
        User user = getLoggedInUser(session);
        try {
            var quizData = quizService.getAllQuizQuestions();
            model.addAttribute("questions", quizData);
            model.addAttribute("totalQuestions", quizData.size());
            model.addAttribute("user", user);
            System.out.println("Loaded " + quizData.size() + " quiz questions");
            return "customer/manage-preference/quiz-questions";
        } catch (Exception e) {
            System.out.println("Error loading quiz questions: " + e.getMessage());
            model.addAttribute("error", "Failed to load quiz questions");
            return "customer/manage-preference/quiz-start";
        }
    }

    // ========== SUBMIT QUIZ ANSWERS & GET RECOMMENDATIONS ==========
    @PostMapping("/submit")
    public String submitQuizAnswers(@RequestParam Map<String, String> answers,
                                    HttpSession session,
                                    Model model) {
        if (!isCustomerLoggedIn(session)) {
            return "redirect:/login";
        }
        User user = getLoggedInUser(session);
        try {
            System.out.println("Processing quiz answers from customer: " + user.getUserId());
            System.out.println("Total answers received: " + answers.size());

            List<Product> recommendations = quizService.getRecommendations(answers);
            List<Product> topRecommendations = recommendations.stream()
                    .limit(3)
                    .toList();

            System.out.println("Generated " + topRecommendations.size() + " recommendations");
            quizService.saveQuizResponse(String.valueOf(user.getUserId()), answers, topRecommendations);
            
            model.addAttribute("recommendations", topRecommendations);
            model.addAttribute("user", user);
            model.addAttribute("totalRecommendations", topRecommendations.size());
            return "customer/manage-preference/quiz-results";
        } catch (Exception e) {
            System.out.println("Error processing quiz: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to process your quiz. Please try again.");
            return "customer/manage-preference/quiz-start";
        }
    }

    // ========== VIEW PAST RECOMMENDATIONS ==========
    @GetMapping("/history")
    public String viewRecommendationHistory(HttpSession session, Model model) {
        if (!isCustomerLoggedIn(session)) {
            return "redirect:/login";
        }
        User user = getLoggedInUser(session);
        try {
            List<Product> pastRecommendations = quizService.getCustomerRecommendationHistory(String.valueOf(user.getUserId()));
            model.addAttribute("recommendations", pastRecommendations);
            model.addAttribute("user", user);
            return "customer/manage-preference/recommendation-history";
        } catch (Exception e) {
            System.out.println("Error loading history: " + e.getMessage());
            model.addAttribute("error", "Failed to load recommendation history");
            return "customer/manage-preference/quiz-start";
        }
    }

    // ========== SUBMIT QUIZ WITH AI-POWERED RECOMMENDATIONS ==========
    @PostMapping("/submit-with-ai")
    public String submitQuizWithAI(@RequestParam Map<String, String> answers,
                                   HttpSession session,
                                   Model model) {
        if (!isCustomerLoggedIn(session)) {
            return "redirect:/login";
        }
        User user = getLoggedInUser(session);
        try {
            System.out.println("Processing quiz answers with AI for customer: " + user.getUserId());
            List<Product> recommendations = quizService.getAIPoweredRecommendations(answers);
            List<Product> topRecommendations = recommendations.stream()
                    .limit(3)
                    .toList();
            
            System.out.println("🤖 Generated " + topRecommendations.size() + " AI-powered recommendations");
            quizService.saveQuizResponse(String.valueOf(user.getUserId()), answers, topRecommendations);

            model.addAttribute("recommendations", topRecommendations);
            model.addAttribute("user", user);
            model.addAttribute("totalRecommendations", topRecommendations.size());
            model.addAttribute("recommendationType", "AI-Powered");

            return "customer/manage-preference/quiz-results";
        } catch (Exception e) {
            System.out.println("❌ Error processing quiz with AI: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to process your quiz with AI. Please try again.");
            return "customer/manage-preference/quiz-start";
        }
    }

    // ========== SUBMIT QUIZ WITH HYBRID RECOMMENDATIONS ==========
    @PostMapping("/submit-hybrid")
    public String submitQuizHybrid(@RequestParam Map<String, String> answers,
                                   HttpSession session,
                                   Model model) {
        if (!isCustomerLoggedIn(session)) {
            return "redirect:/login";
        }
        User user = getLoggedInUser(session);
        try {
            System.out.println("Processing quiz answers with Hybrid (AI + Traditional) for customer: " + user.getUserId());
            List<Product> recommendations = quizService.getHybridRecommendations(answers);
            List<Product> topRecommendations = recommendations.stream()
                    .limit(3)
                    .toList();

            System.out.println("⚡ Generated " + topRecommendations.size() + " hybrid recommendations");
            quizService.saveQuizResponse(String.valueOf(user.getUserId()), answers, topRecommendations);

            model.addAttribute("recommendations", topRecommendations);
            model.addAttribute("user", user);
            model.addAttribute("totalRecommendations", topRecommendations.size());
            model.addAttribute("recommendationType", "Hybrid (AI-Enhanced)");

            return "customer/manage-preference/quiz-results";
        } catch (Exception e) {
            System.out.println("❌ Error processing quiz with hybrid approach: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to process your quiz. Please try again.");
            return "customer/manage-preference/quiz-start";
        }
    }

    // ========== SUBMIT PERSONA QUIZ (FIXED) ==========
    @PostMapping("/submit-persona")
    public String submitPersonaQuiz(@RequestParam Map<String, String> answers,
                                    HttpSession session,
                                    Model model) {
        if (!isCustomerLoggedIn(session)) {
            return "redirect:/login";
        }
        User user = getLoggedInUser(session);

        try {
            System.out.println("🎨 Processing persona quiz answers from customer: " + user.getUserId());
            System.out.println("📝 Answers received: " + answers);

            // Calculate persona scores
            Map<String, Integer> personaScores = quizService.calculatePersonaScores(answers);
            String primaryPersona = quizService.getPrimaryPersona(answers);
            
            System.out.println("✅ Primary Persona: " + primaryPersona);
            System.out.println("📊 Persona Scores: " + personaScores);

            // ✅ FIXED: Pass ALL THREE parameters (answers, primaryPersona, personaScores)
            List<Product> aiRecommendations = quizService.getPersonaBasedAIRecommendations(
                answers,           // Parameter 1: All quiz answers
                primaryPersona,    // Parameter 2: Primary persona name
                personaScores      // Parameter 3: Persona score map
            );
            
            List<Product> topRecommendations = aiRecommendations.stream()
                    .limit(3)
                    .toList();

            System.out.println("🤖 AI generated " + topRecommendations.size() + " perfume recommendations");
            
            if (topRecommendations.isEmpty()) {
                System.out.println("⚠️ WARNING: No recommendations generated. Check database and API key.");
            } else {
                for (Product p : topRecommendations) {
                    System.out.println("  → Recommended: " + p.getProductName());
                }
            }

            // Add attributes for the view
            model.addAttribute("primaryPersona", primaryPersona);
            model.addAttribute("personaScores", personaScores);
            
            // Add individual scores for easy access in Thymeleaf
            model.addAttribute("freshScore", personaScores.getOrDefault("Fresh & Airy", 0));
            model.addAttribute("warmScore", personaScores.getOrDefault("Warm & Cozy", 0));
            model.addAttribute("floralScore", personaScores.getOrDefault("Floral & Romantic", 0));
            model.addAttribute("woodyScore", personaScores.getOrDefault("Woody & Earthy", 0));
            model.addAttribute("boldScore", personaScores.getOrDefault("Bold & Spicy", 0));
            
            model.addAttribute("recommendations", topRecommendations);
            model.addAttribute("user", user);
            model.addAttribute("totalRecommendations", topRecommendations.size());

            return "customer/manage-preference/persona-results";

        } catch (Exception e) {
            System.out.println("❌ Error processing persona quiz: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to process your quiz. Please try again.");
            return "customer/manage-preference/quiz-start";
        }
    }
}