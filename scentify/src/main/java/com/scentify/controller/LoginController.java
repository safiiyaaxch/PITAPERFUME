package com.scentify.controller;

import com.scentify.model.*;
import com.scentify.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
public class LoginController {

    @Autowired private UserRepository userRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private SupplierRepository supplierRepository;

    // ================= HOME =================

    @GetMapping("/")
    public String home() {
        return "index";
    }

    // ================= LOGIN =================

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {

        if (error != null) model.addAttribute("error", "Invalid username or password");
        if (logout != null) model.addAttribute("message", "Logged out successfully");

        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes redirect) {

        User user = userRepository.findByUsername(username).orElse(null);

        System.out.println("========== LOGIN ATTEMPT ==========");
        System.out.println("Username: " + username);
        System.out.println("User found: " + (user != null));
        if (user != null) {
            System.out.println("User ID: " + user.getUserId());
            System.out.println("User Role: " + user.getRole());
            System.out.println("Password match: " + user.getPassword().equals(password));
        }
        System.out.println("====================================");

        if (user == null || !user.getPassword().equals(password)) {
            redirect.addFlashAttribute("error", "Invalid username or password");
            return "redirect:/login";
        }

        session.setAttribute("loggedInUser", user);

        String role = user.getRole().toLowerCase();
        System.out.println("User logged in with role: " + role);

        return switch (role) {
            case "customer" -> "redirect:/customer/dashboard";
            case "supplier" -> "redirect:/supplier/dashboard";
            case "manager" -> "redirect:/manager/dashboard";
            default -> {
                System.out.println("Unknown role: " + role);
                yield "redirect:/login";
            }
        };
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout=true";
    }

    // ================= SIGNUP =================

    @GetMapping("/signup")
    public String signupPage(@RequestParam(required = false) String error,
                             @RequestParam(required = false) String success,
                             Model model) {

        if (error != null) {
            model.addAttribute("error", switch (error) {
                case "password-mismatch" -> "Passwords do not match";
                case "password-short" -> "Password must be at least 6 characters";
                case "username-taken" -> "Username already exists";
                case "email-taken" -> "Email already registered";
                case "invalid-role" -> "Invalid role selected";
                case "missing-document" -> "Business proof is required";
                case "invalid-document" -> "Only PDF files allowed";
                default -> "Registration failed";
            });
        }

        if (success != null) {
            model.addAttribute("success", "Registration successful! Please login.");
        }

        return "signup";
    }

    // ================= SIGNUP POST =================

    @PostMapping("/signup")
    @Transactional
    public String signup(@RequestParam String fullname,
                         @RequestParam String username,
                         @RequestParam String email,
                         @RequestParam String password,
                         @RequestParam String confirmPassword,
                         @RequestParam String role,
                         @RequestParam(required = false) MultipartFile businessProof) {

        role = role.trim().toLowerCase();

        // 🔒 validations
        if (!password.equals(confirmPassword))
            return "redirect:/signup?error=password-mismatch";

        if (password.length() < 6)
            return "redirect:/signup?error=password-short";

        if (userRepository.existsByUsername(username))
            return "redirect:/signup?error=username-taken";

        if (userRepository.existsByEmail(email))
            return "redirect:/signup?error=email-taken";

        if (!role.equals("customer") && !role.equals("supplier") && !role.equals("system_manager"))
            return "redirect:/signup?error=invalid-role";

        // supplier file validation FIRST
        String savedFilename = null;
        if (role.equals("supplier")) {
            if (businessProof == null || businessProof.isEmpty())
                return "redirect:/signup?error=missing-document";

            if (!businessProof.getOriginalFilename().toLowerCase().endsWith(".pdf"))
                return "redirect:/signup?error=invalid-document";
        }

        // create user
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password); // later replace with BCrypt
        user.setRole(role);

        userRepository.save(user);

        // role-specific
        if (role.equals("customer")) {
            Customer customer = new Customer();
            customer.setUser(user);
            customer.setFullname(fullname);
            customer.setLoyaltyPoints(0);
            customerRepository.save(customer);
        }

        if (role.equals("supplier")) {
            savedFilename = saveBusinessProof(businessProof, username);

            Supplier supplier = new Supplier();
            supplier.setUser(user);
            supplier.setBrandName(fullname);
            supplier.setApprovalStatus("pending");
            supplier.setBusinessRegistration(savedFilename);
            supplierRepository.save(supplier);
        }

        if (role.equals("manager")) {
            // Manager is automatically approved
        }

        return "redirect:/signup?success=true";
    }

    // ================= FILE SAVE =================

    private String saveBusinessProof(MultipartFile file, String username) {

        try {
            String uploadDir = "uploads/business-docs/";
            new File(uploadDir).mkdirs();

            String filename = username + "_" + UUID.randomUUID() + ".pdf";
            Path path = Paths.get(uploadDir + filename);
            Files.write(path, file.getBytes());

            return filename;

        } catch (IOException e) {
            throw new RuntimeException("File upload failed");
        }
    }
}
