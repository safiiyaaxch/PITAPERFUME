package com.scentify.controller;

import com.scentify.model.User;
import com.scentify.model.Product;
import com.scentify.repository.ProductRepository;
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
    
    @Autowired
    private ProductRepository productRepository;
    
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
            
            System.out.println("📝 Loaded " + quizData.size() + " quiz questions");
            
            return "customer/manage-preference/quiz-questions";
        } catch (Exception e) {
            System.out.println("❌ Error loading quiz questions: " + e.getMessage());
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
            System.out.println("📊 Processing quiz answers from customer: " + user.getUserId());
            System.out.println("📝 Total answers received: " + answers.size());
            
            // Get recommendations from ML service
            List<Product> recommendations = quizService.getRecommendations(answers);
            
            // Get top 3 recommendations
            List<Product> topRecommendations = recommendations.stream()
                    .limit(3)
                    .toList();
            
            System.out.println("✅ Generated " + topRecommendations.size() + " recommendations");
            
            // Save quiz response to database for future ML training
            quizService.saveQuizResponse(String.valueOf(user.getUserId()), answers, topRecommendations);
            
            model.addAttribute("recommendations", topRecommendations);
            model.addAttribute("user", user);
            model.addAttribute("totalRecommendations", topRecommendations.size());
            
            return "customer/manage-preference/quiz-results";
            
        } catch (Exception e) {
            System.out.println("❌ Error processing quiz: " + e.getMessage());
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
            System.out.println("❌ Error loading history: " + e.getMessage());
            model.addAttribute("error", "Failed to load recommendation history");
            return "customer/manage-preference/quiz-start";
        }
    }
}
