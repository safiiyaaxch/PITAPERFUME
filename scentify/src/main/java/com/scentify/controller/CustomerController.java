package com.scentify.controller;

import com.scentify.model.Customer;
import com.scentify.model.Product;
import com.scentify.model.PromotionVoucher;
import com.scentify.model.User;
import com.scentify.repository.CustomerRepository;
import com.scentify.repository.ProductRepository;
import com.scentify.repository.PromotionVoucherRepository;
import com.scentify.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/customer")
public class CustomerController {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PromotionVoucherRepository promotionVoucherRepository;
    
    // ========== SESSION VALIDATION HELPER ==========
    private boolean isCustomerLoggedIn(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        return user != null && "customer".equalsIgnoreCase(user.getRole());
    }
    
    private User getLoggedInUser(HttpSession session) {
        return (User) session.getAttribute("loggedInUser");
    }
    
    // ========== DASHBOARD ==========
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model, 
                           @RequestParam(required = false) String search,
                           @RequestParam(required = false) String category) {
        
        System.out.println("========== CUSTOMER DASHBOARD ==========");
        User user = getLoggedInUser(session);
        System.out.println("Session user: " + user);
        
        if (!isCustomerLoggedIn(session)) {
            System.out.println("Customer not logged in, redirecting to login");
            return "redirect:/login";
        }
        
        // Get all approved products
        List<Product> approvedProducts = productRepository.findByApprovalStatus("approved");
        System.out.println("Found " + approvedProducts.size() + " approved products");
        
        // Filter by category if provided
        if (category != null && !category.isEmpty()) {
            approvedProducts = approvedProducts.stream()
                    .filter(p -> p.getCategoryId().equalsIgnoreCase(category))
                    .toList();
            System.out.println("After category filter: " + approvedProducts.size() + " products");
        }
        
        // Filter by search term if provided
        if (search != null && !search.isEmpty()) {
            String searchTerm = search.toLowerCase();
            approvedProducts = approvedProducts.stream()
                    .filter(p -> p.getProductName().toLowerCase().contains(searchTerm) ||
                               p.getDescription().toLowerCase().contains(searchTerm))
                    .toList();
            System.out.println("After search filter: " + approvedProducts.size() + " products");
        }
        
        // Get customer details
        Optional<Customer> customerOpt = customerRepository.findByUser(user);
        if (customerOpt.isPresent()) {
            model.addAttribute("customer", customerOpt.get());
        }
        
        model.addAttribute("user", user);
        model.addAttribute("approvedProducts", approvedProducts);
        model.addAttribute("search", search);
        model.addAttribute("category", category);
        
        System.out.println("========================================");
        return "customer/dashboard";
    }
    
    // ========== PRODUCT DETAILS ==========
    @GetMapping("/product/{id}")
    public String productDetails(@PathVariable String id, 
                                 HttpSession session, 
                                 Model model) {
        
        if (!isCustomerLoggedIn(session)) {
            return "redirect:/login";
        }
        
        Optional<Product> productOpt = productRepository.findById(id);
        
        if (productOpt.isEmpty() || !productOpt.get().getApprovalStatus().equalsIgnoreCase("approved")) {
            return "redirect:/customer/dashboard?error=Product not found";
        }
        
        Product product = productOpt.get();
        
        // Get supplier username
        Optional<User> supplierOpt = userRepository.findById(product.getUserId());
        if (supplierOpt.isPresent()) {
            model.addAttribute("supplierUsername", supplierOpt.get().getUsername());
        }
        
        model.addAttribute("product", product);
        model.addAttribute("user", getLoggedInUser(session));
        
        return "customer/product-details";
    }
    
    // ========== SEARCH API ==========
    @PostMapping("/api/search-products")
    @ResponseBody
    public List<Product> searchProducts(@RequestParam(required = false) String query,
                                       @RequestParam(required = false) String category,
                                       HttpSession session) {
        
        if (!isCustomerLoggedIn(session)) {
            return List.of();
        }
        
        List<Product> products = productRepository.findByApprovalStatus("approved");
        
        if (category != null && !category.isEmpty()) {
            products = products.stream()
                    .filter(p -> p.getCategoryId().equalsIgnoreCase(category))
                    .toList();
        }
        
        if (query != null && !query.isEmpty()) {
            String searchTerm = query.toLowerCase();
            products = products.stream()
                    .filter(p -> p.getProductName().toLowerCase().contains(searchTerm) ||
                               p.getDescription().toLowerCase().contains(searchTerm))
                    .toList();
        }
        
        return products;
    }

    // ========== VIEW AVAILABLE VOUCHERS ==========
    @GetMapping("/vouchers")
    public String viewVouchers(HttpSession session, Model model) {
        
        if (!isCustomerLoggedIn(session)) {
            return "redirect:/login";
        }
        
        User user = getLoggedInUser(session);
        
        // Get customer details
        Optional<Customer> customerOpt = customerRepository.findByUser(user);
        if (customerOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        Customer customer = customerOpt.get();
        model.addAttribute("customer", customer);
        
        // Only show vouchers if customer has active membership
        List<PromotionVoucher> vouchers = List.of();
        if (customer.getIsMember()) {
            // Get all active and non-expired vouchers
            vouchers = promotionVoucherRepository.findAll().stream()
                    .filter(v -> v.getEndDate() != null && v.getEndDate().isAfter(LocalDateTime.now()))
                    .filter(v -> !v.getIsActive() || v.getIsActive())  // Include active vouchers
                    .toList();
        }
        
        model.addAttribute("vouchers", vouchers);
        model.addAttribute("user", user);
        
        return "customer/select-voucher";
    }

    // ========== CHECKOUT ==========
    @GetMapping("/checkout")
    public String checkout(@RequestParam String productId,
                          HttpSession session, 
                          Model model) {
        
        if (!isCustomerLoggedIn(session)) {
            return "redirect:/login";
        }
        
        User user = getLoggedInUser(session);
        
        // Get product
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return "redirect:/customer/dashboard?error=Product not found";
        }
        
        Product product = productOpt.get();
        
        // Get customer details
        Optional<Customer> customerOpt = customerRepository.findByUser(user);
        if (customerOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        Customer customer = customerOpt.get();
        
        // Get available vouchers if customer is member
        List<PromotionVoucher> availableVouchers = List.of();
        if (customer.getIsMember()) {
            availableVouchers = promotionVoucherRepository.findAll().stream()
                    .filter(v -> v.getEndDate() != null && v.getEndDate().isAfter(LocalDateTime.now()))
                    .filter(v -> v.getIsActive())
                    .toList();
        }
        
        model.addAttribute("product", product);
        model.addAttribute("customer", customer);
        model.addAttribute("user", user);
        model.addAttribute("availableVouchers", availableVouchers);
        
        return "customer/checkout";
    }
}

