package com.scentify.controller;

import com.scentify.model.Product;
import com.scentify.model.User;
import com.scentify.repository.ProductRepository;
import com.scentify.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/manager")
public class ManagerController {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // ========== SESSION VALIDATION HELPER ==========
    private boolean isManagerLoggedIn(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        return user != null && "system_manager".equalsIgnoreCase(user.getRole());
    }
    
    private User getLoggedInUser(HttpSession session) {
        return (User) session.getAttribute("loggedInUser");
    }
    
    // ========== DASHBOARD ==========
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!isManagerLoggedIn(session)) {
            return "redirect:/login";
        }
        
        // Get all pending products across all suppliers
        List<Product> pendingProducts = productRepository.findByApprovalStatus("pending");
        List<Product> approvedProducts = productRepository.findByApprovalStatus("approved");
        List<Product> rejectedProducts = productRepository.findByApprovalStatus("rejected");
        
        // Add supplier name to each product for display
        for (Product product : pendingProducts) {
            Optional<User> user = userRepository.findById(product.getUserId());
            if (user.isPresent()) {
                product.setSupplierName(user.get().getUsername());
            } else {
                product.setSupplierName("Unknown");
            }
        }
        
        for (Product product : approvedProducts) {
            Optional<User> user = userRepository.findById(product.getUserId());
            if (user.isPresent()) {
                product.setSupplierName(user.get().getUsername());
            } else {
                product.setSupplierName("Unknown");
            }
        }
        
        for (Product product : rejectedProducts) {
            Optional<User> user = userRepository.findById(product.getUserId());
            if (user.isPresent()) {
                product.setSupplierName(user.get().getUsername());
            } else {
                product.setSupplierName("Unknown");
            }
        }
        
        model.addAttribute("pendingProducts", pendingProducts);
        model.addAttribute("approvedProducts", approvedProducts);
        model.addAttribute("rejectedProducts", rejectedProducts);
        
        System.out.println("Manager Dashboard - Pending: " + pendingProducts.size() + 
                          ", Approved: " + approvedProducts.size() + 
                          ", Rejected: " + rejectedProducts.size());
        
        return "manager/products-dashboard";
    }
    
    // ========== APPROVE PRODUCT ==========
    @PostMapping("/products/approve/{id}")
    public String approveProduct(@PathVariable String id,
                                 HttpSession session,
                                 RedirectAttributes redirect) {
        if (!isManagerLoggedIn(session)) {
            return "redirect:/login";
        }
        
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) {
            redirect.addFlashAttribute("error", "Product not found");
            return "redirect:/manager/dashboard";
        }
        
        try {
            Product product = productOpt.get();
            product.setApprovalStatus("approved");
            product.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product);
            
            // Get supplier info for message
            Optional<User> supplier = userRepository.findById(product.getUserId());
            String supplierName = supplier.isPresent() ? supplier.get().getUsername() : "Supplier";
            
            redirect.addFlashAttribute("success", "Product '" + product.getProductName() + 
                    "' from " + supplierName + " has been approved!");
            
            System.out.println("Product approved: " + id + " by manager: " + 
                    getLoggedInUser(session).getUserId());
        } catch (Exception e) {
            System.out.println("Error approving product: " + e.getMessage());
            redirect.addFlashAttribute("error", "Failed to approve product");
        }
        
        return "redirect:/manager/dashboard";
    }
    
    // ========== REJECT PRODUCT ==========
    @PostMapping("/products/reject/{id}")
    public String rejectProduct(@PathVariable String id,
                                HttpSession session,
                                RedirectAttributes redirect) {
        if (!isManagerLoggedIn(session)) {
            return "redirect:/login";
        }
        
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) {
            redirect.addFlashAttribute("error", "Product not found");
            return "redirect:/manager/dashboard";
        }
        
        try {
            Product product = productOpt.get();
            product.setApprovalStatus("rejected");
            product.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product);
            
            // Get supplier info for message
            Optional<User> supplier = userRepository.findById(product.getUserId());
            String supplierName = supplier.isPresent() ? supplier.get().getUsername() : "Supplier";
            
            redirect.addFlashAttribute("warning", "Product '" + product.getProductName() + 
                    "' from " + supplierName + " has been rejected!");
            
            System.out.println("Product rejected: " + id + " by manager: " + 
                    getLoggedInUser(session).getUserId());
        } catch (Exception e) {
            System.out.println("Error rejecting product: " + e.getMessage());
            redirect.addFlashAttribute("error", "Failed to reject product");
        }
        
        return "redirect:/manager/dashboard";
    }
    
    // ========== VIEW PRODUCT DETAILS ==========
    @GetMapping("/products/view/{id}")
    public String viewProduct(@PathVariable String id,
                             HttpSession session,
                             Model model) {
        if (!isManagerLoggedIn(session)) {
            return "redirect:/login";
        }
        
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) {
            return "redirect:/manager/dashboard";
        }
        
        Product product = productOpt.get();
        Optional<User> supplier = userRepository.findById(product.getUserId());
        
        model.addAttribute("product", product);
        if (supplier.isPresent()) {
            model.addAttribute("supplier", supplier.get());
        }
        
        return "manager/product-details";
    }
}
