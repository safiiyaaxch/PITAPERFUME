package com.scentify;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {
    
    @GetMapping("/login")
    public String showLoginPage() {
        System.out.println("GET /login called - showing login page");
        return "login";
    }
    
    @PostMapping("/login")
    public String handleLogin(
            @RequestParam String username,
            @RequestParam String password,
            Model model) {
        
        System.out.println("POST /login called");
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);
        
        // Dummy check
        if ("admin".equals(username) && "password".equals(password)) {
            System.out.println("Login SUCCESS - redirecting to /home");
            return "redirect:/home";
        } else {
            System.out.println("Login FAILED - showing error");
            model.addAttribute("error", true);
            return "login";
        }
    }
    
    @GetMapping("/home")
    public String home() {
        System.out.println("GET /home called - showing home page");
        return "home";
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        System.out.println("GET /register called - showing register page");
        return "register";
    }

    @PostMapping("/register")
    public String handleRegister(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String username,
            @RequestParam String password,
            Model model) {
        
        System.out.println("POST /register called");
        System.out.println("Name: " + name);
        System.out.println("Email: " + email);
        System.out.println("Username: " + username);
        
        System.out.println("Registration successful - redirecting to /login");
        return "redirect:/login";
    }

}