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
            return "customer/manage-preference/quiz-questions";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load quiz questions");
            return "customer/manage-preference/quiz-start";
        }
    }

    // ========== SUBMIT PERSONA QUIZ - MAIN METHOD ==========
    @PostMapping("/submit-persona")
    public String submitPersonaQuiz(@RequestParam Map<String, String> answers,
                                    HttpSession session,
                                    Model model) {
        if (!isCustomerLoggedIn(session)) {
            return "redirect:/login";
        }
        User user = getLoggedInUser(session);

        try {
            System.out.println("🎨 Processing persona quiz for user: " + user.getUserId());
            System.out.println("📝 Answers received: " + answers);

            // Calculate persona scores
            Map<String, Integer> personaScores = quizService.calculatePersonaScores(answers);
            String primaryPersona = quizService.getPrimaryPersona(answers);
            
            System.out.println("✅ Primary Persona: " + primaryPersona);
            System.out.println("📊 Persona Scores: " + personaScores);

            // Get recommendations using rule-based engine (NO API)
            List<Product> recommendations = quizService.getPersonaBasedRecommendations(
                answers,         
                primaryPersona,  
                personaScores    
            );
            
            List<Product> topRecommendations = recommendations.stream()
                    .limit(3)
                    .collect(java.util.stream.Collectors.toList());

            System.out.println("📦 Generated " + topRecommendations.size() + " recommendations");
            for (Product p : topRecommendations) {
                System.out.println("  → " + p.getProductName() + " (Score: computed)");
            }

            // Add attributes for the view
            model.addAttribute("primaryPersona", primaryPersona);
            model.addAttribute("personaScores", personaScores);
            model.addAttribute("recommendations", topRecommendations);
            model.addAttribute("user", user);
            model.addAttribute("totalRecommendations", topRecommendations.size());
            model.addAttribute("recommendationType", "✨ Persona-Based Matching");

            return "customer/manage-preference/persona-results";

        } catch (Exception e) {
            System.out.println("❌ Error processing persona quiz: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to process your quiz. Please try again.");
            return "customer/manage-preference/quiz-start";
        }
    }

    // ========== LEGACY METHODS (Keep for compatibility) ==========
    
    @PostMapping("/submit")
    public String submitQuizAnswers(@RequestParam Map<String, String> answers,
                                    HttpSession session,
                                    Model model) {
        if (!isCustomerLoggedIn(session)) {
            return "redirect:/login";
        }
        User user = getLoggedInUser(session);
        try {
            List<Product> recommendations = quizService.getRecommendations(answers);
            List<Product> topRecommendations = recommendations.stream()
                    .limit(3)
                    .collect(java.util.stream.Collectors.toList());

            model.addAttribute("recommendations", topRecommendations);
            model.addAttribute("user", user);
            model.addAttribute("recommendationType", "📋 Standard Matching");
            return "customer/manage-preference/quiz-results";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to process your quiz. Please try again.");
            return "customer/manage-preference/quiz-start";
        }
    }

    @PostMapping("/submit-with-ai")
    public String submitQuizWithAI(@RequestParam Map<String, String> answers,
                                   HttpSession session,
                                   Model model) {
        return submitPersonaQuiz(answers, session, model);
    }

    @PostMapping("/submit-hybrid")
    public String submitQuizHybrid(@RequestParam Map<String, String> answers,
                                   HttpSession session,
                                   Model model) {
        return submitPersonaQuiz(answers, session, model);
    }

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
            model.addAttribute("error", "Failed to load recommendation history");
            return "customer/manage-preference/quiz-start";
        }
    }
}