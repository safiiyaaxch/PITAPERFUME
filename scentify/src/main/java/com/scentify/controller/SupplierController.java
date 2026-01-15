package com.scentify.controller;

import com.scentify.model.Product;
import com.scentify.model.User;
import com.scentify.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/supplier")
public class SupplierController {
    
    @Autowired
    private ProductRepository productRepository;
    
    // ========== SESSION VALIDATION HELPER ==========
    private boolean isSupplierLoggedIn(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        return user != null && "supplier".equalsIgnoreCase(user.getRole());
    }
    
    private User getLoggedInUser(HttpSession session) {
        return (User) session.getAttribute("loggedInUser");
    }
    
    // ========== DASHBOARD ==========
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        System.out.println("========== SUPPLIER DASHBOARD ==========");
        System.out.println("Session user: " + user);
        if (user != null) {
            System.out.println("User ID: " + user.getUserId());
            System.out.println("User Role: " + user.getRole());
            System.out.println("Is supplier: " + "supplier".equalsIgnoreCase(user.getRole()));
        }
        System.out.println("========================================");
        
        if (!isSupplierLoggedIn(session)) {
            System.out.println("Supplier not logged in, redirecting to login");
            return "redirect:/login";
        }

        User loggedInUser = getLoggedInUser(session);
        List<Product> products = productRepository.findByUserId(loggedInUser.getUserId());
        System.out.println("Found " + products.size() + " products for user " + loggedInUser.getUserId());
        model.addAttribute("products", products);
        model.addAttribute("user", loggedInUser);  // Add user to model for template
        return "supplier/dashboard";
    }
    
    // ========== FILE UPLOAD ==========
    @PostMapping("/upload-image")
    @ResponseBody
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file,
                                         HttpSession session) {
        if (!isSupplierLoggedIn(session)) {
            return ResponseEntity.status(401).body("{\"error\": \"Unauthorized\"}");
        }
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\": \"No file selected\"}");
        }
        
        try {
            // Validate file type
            String contentType = file.getContentType();
            if (!contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body("{\"error\": \"Only image files are allowed\"}");
            }
            
            // Create uploads directory if it doesn't exist
            Path uploadsDir = Paths.get("uploads/products");
            Files.createDirectories(uploadsDir);
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadsDir.resolve(uniqueFilename);
            
            // Save file
            Files.write(filePath, file.getBytes());
            
            // Return user-friendly URL
            String imageUrl = "/uploads/products/" + uniqueFilename;
            return ResponseEntity.ok("{\"success\": true, \"imageUrl\": \"" + imageUrl + "\", \"filename\": \"" + uniqueFilename + "\"}");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("{\"error\": \"File upload failed: " + e.getMessage() + "\"}");
        }
    }
    
    // ========== ADD PRODUCT ==========
    @GetMapping("/products/add")
    public String showAddForm(Model model, HttpSession session) {
        if (!isSupplierLoggedIn(session)) {
            return "redirect:/login";
        }
        model.addAttribute("product", new Product());
        return "supplier/add-product";
    }
    
    @PostMapping("/products/add")
    public String addProduct(@ModelAttribute Product product, 
                           RedirectAttributes redirect,
                           HttpSession session) {
        if (!isSupplierLoggedIn(session)) {
            return "redirect:/login";
        }
        
        User user = getLoggedInUser(session);
        
        try {
            // Generate product ID if not provided (P01, P02, P03... up to P99)
            if (product.getProductId() == null || product.getProductId().trim().isEmpty()) {
                long count = productRepository.count();
                // Format: P + 2 digits (P01 to P99)
                product.setProductId("P" + String.format("%02d", (count % 99) + 1));
            }
            
            product.setUserId(user.getUserId());
            product.setApprovalStatus("pending");
            product.setPrice(product.getPrice() != null ? product.getPrice() : 0.0);
            product.setStock(product.getStock() != null ? product.getStock() : 0);
            
            // Ensure timestamps are set
            if (product.getCreatedAt() == null) {
                product.setCreatedAt(java.time.LocalDateTime.now());
            }
            if (product.getUpdatedAt() == null) {
                product.setUpdatedAt(java.time.LocalDateTime.now());
            }
            
            productRepository.save(product);
            redirect.addFlashAttribute("success", "Product added successfully!");
            System.out.println("Product added: " + product.getProductId() + " by user: " + user.getUserId());
        } catch (Exception e) {
            System.out.println("Error adding product: " + e.getMessage());
            e.printStackTrace();
            redirect.addFlashAttribute("error", "Failed to add product: " + e.getMessage());
        }
        return "redirect:/supplier/dashboard";
    }
    
    // ========== EDIT PRODUCT ==========
    @GetMapping("/products/edit/{id}")
    public String showEditForm(@PathVariable String id, 
                              Model model, 
                              HttpSession session) {
        if (!isSupplierLoggedIn(session)) {
            return "redirect:/login";
        }
        
        User user = getLoggedInUser(session);
        Optional<Product> productOpt = productRepository.findById(id);
        
        if (productOpt.isEmpty() || !productOpt.get().getUserId().equals(user.getUserId())) {
            return "redirect:/supplier/dashboard";
        }
        
        model.addAttribute("product", productOpt.get());
        return "supplier/edit-product";
    }
    
    @PostMapping("/products/edit/{id}")
    public String updateProduct(@PathVariable String id, 
                               @ModelAttribute Product productDetails, 
                               RedirectAttributes redirect,
                               HttpSession session) {
        if (!isSupplierLoggedIn(session)) {
            return "redirect:/login";
        }
        
        User user = getLoggedInUser(session);
        Optional<Product> productOpt = productRepository.findById(id);
        
        if (productOpt.isEmpty() || !productOpt.get().getUserId().equals(user.getUserId())) {
            redirect.addFlashAttribute("error", "Unauthorized");
            return "redirect:/supplier/dashboard";
        }
        
        try {
            Product product = productOpt.get();
            product.setProductName(productDetails.getProductName());
            product.setDescription(productDetails.getDescription());
            product.setCategoryId(productDetails.getCategoryId());
            product.setProdimage(productDetails.getProdimage());
            product.setPrice(productDetails.getPrice() != null ? productDetails.getPrice() : product.getPrice());
            product.setStock(productDetails.getStock() != null ? productDetails.getStock() : product.getStock());
            product.setApprovalStatus(productDetails.getApprovalStatus());
            
            productRepository.save(product);
            redirect.addFlashAttribute("success", "Product updated successfully!");
            System.out.println("Product updated: " + id + " by user: " + user.getUserId());
        } catch (Exception e) {
            System.out.println("Error updating product: " + e.getMessage());
            redirect.addFlashAttribute("error", "Failed to update product");
        }
        return "redirect:/supplier/dashboard";
    }
    
    // ========== DELETE PRODUCT ==========
    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable String id, 
                               RedirectAttributes redirect,
                               HttpSession session) {
        if (!isSupplierLoggedIn(session)) {
            return "redirect:/login";
        }
        
        User user = getLoggedInUser(session);
        Optional<Product> productOpt = productRepository.findById(id);
        
        if (productOpt.isEmpty() || !productOpt.get().getUserId().equals(user.getUserId())) {
            redirect.addFlashAttribute("error", "Unauthorized");
            return "redirect:/supplier/dashboard";
        }
        
        try {
            productRepository.deleteById(id);
            redirect.addFlashAttribute("success", "Product deleted successfully!");
            System.out.println("Product deleted: " + id + " by user: " + user.getUserId());
        } catch (Exception e) {
            System.out.println("Error deleting product: " + e.getMessage());
            redirect.addFlashAttribute("error", "Failed to delete product");
        }
        return "redirect:/supplier/dashboard";
    }
    
    // ========== VIEW PRODUCT ==========
    @GetMapping("/products/view/{id}")
    public String viewProduct(@PathVariable String id, 
                             Model model,
                             HttpSession session) {
        if (!isSupplierLoggedIn(session)) {
            return "redirect:/login";
        }
        
        User user = getLoggedInUser(session);
        Optional<Product> productOpt = productRepository.findById(id);
        
        if (productOpt.isEmpty() || !productOpt.get().getUserId().equals(user.getUserId())) {
            return "redirect:/supplier/dashboard";
        }
        
        model.addAttribute("product", productOpt.get());
        return "supplier/view-product";
    }
}