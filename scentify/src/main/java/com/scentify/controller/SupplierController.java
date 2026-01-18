package com.scentify.controller;

import com.scentify.model.Product;
import com.scentify.model.User;
import com.scentify.model.PromotionVoucher;
import com.scentify.model.VoucherProduct;
import com.scentify.repository.ProductRepository;
import com.scentify.repository.PromotionVoucherRepository;
import com.scentify.repository.VoucherProductRepository;
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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/supplier")
public class SupplierController {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private PromotionVoucherRepository promotionVoucherRepository;
    
    @Autowired
    private VoucherProductRepository voucherProductRepository;
    
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
    public String mainDashboard(HttpSession session, Model model) {
        if (!isSupplierLoggedIn(session)) {
            return "redirect:/login";
        }
        
        User loggedInUser = getLoggedInUser(session);
        
        // Get total products count
        List<Product> allProducts = productRepository.findByUserId(loggedInUser.getUserId());
        int totalProducts = allProducts.size();
        
        // Get total active vouchers count
        List<PromotionVoucher> allVouchers = promotionVoucherRepository.findBySupplierIdAndIsActive(loggedInUser.getUserId(), true);
        int totalVouchers = allVouchers.size();
        
        model.addAttribute("user", loggedInUser);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalVouchers", totalVouchers);
        
        return "supplier/main-dashboard";
    }
    
    @GetMapping("/products")
    public String productsDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        System.out.println("========== SUPPLIER PRODUCTS DASHBOARD ==========");
        System.out.println("Session user: " + user);
        if (user != null) {
            System.out.println("User ID: " + user.getUserId());
            System.out.println("User Role: " + user.getRole());
            System.out.println("Is supplier: " + "supplier".equalsIgnoreCase(user.getRole()));
        }
        System.out.println("===============================================");
        
        if (!isSupplierLoggedIn(session)) {
            System.out.println("Supplier not logged in, redirecting to login");
            return "redirect:/login";
        }

        User loggedInUser = getLoggedInUser(session);
        List<Product> products = productRepository.findByUserId(loggedInUser.getUserId());
        System.out.println("Found " + products.size() + " products for user " + loggedInUser.getUserId());
        
        // Separate products by approval status
        List<Product> pendingProducts = productRepository.findByUserIdAndApprovalStatus(loggedInUser.getUserId(), "pending");
        List<Product> approvedProducts = productRepository.findByUserIdAndApprovalStatus(loggedInUser.getUserId(), "approved");
        List<Product> rejectedProducts = productRepository.findByUserIdAndApprovalStatus(loggedInUser.getUserId(), "rejected");
        
        model.addAttribute("products", products);
        model.addAttribute("pendingProducts", pendingProducts);
        model.addAttribute("approvedProducts", approvedProducts);
        model.addAttribute("rejectedProducts", rejectedProducts);
        model.addAttribute("user", loggedInUser);  // Add user to model for template
        return "supplier/manage-product/dashboard";
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
        return "supplier/manage-product/add-product";
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
        return "supplier/manage-product/edit-product";
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
        return "supplier/manage-product/view-product";
    }
    
    // ========== VOUCHER MANAGEMENT ==========
    @GetMapping("/vouchers")
    public String vouchersDashboard(Model model, HttpSession session) {
        if (!isSupplierLoggedIn(session)) {
            return "redirect:/login";
        }
        
        User user = getLoggedInUser(session);
        List<PromotionVoucher> vouchers = promotionVoucherRepository.findBySupplierId(user.getUserId());
        
        // Calculate stats
        int totalVouchers = vouchers.size();
        int activeVouchers = (int) vouchers.stream().filter(v -> v.isValid()).count();
        int totalRedemptions = (int) vouchers.stream().mapToInt(PromotionVoucher::getCurrentUsage).sum();
        
        model.addAttribute("vouchers", vouchers);
        model.addAttribute("totalVouchers", totalVouchers);
        model.addAttribute("activeVouchers", activeVouchers);
        model.addAttribute("totalRedemptions", totalRedemptions);
        
        return "supplier/manage-voucher/vouchers-dashboard";
    }
    
    @GetMapping("/vouchers/add")
    public String addVoucherForm(Model model, HttpSession session) {
        if (!isSupplierLoggedIn(session)) {
            return "redirect:/login";
        }
        
        model.addAttribute("voucher", new PromotionVoucher());
        model.addAttribute("isEdit", false);
        return "supplier/manage-voucher/add-voucher";
    }
    
    @PostMapping("/vouchers/add")
    public String addVoucher(@ModelAttribute PromotionVoucher voucher,
                            RedirectAttributes redirectAttributes,
                            HttpSession session) {
        if (!isSupplierLoggedIn(session)) {
            return "redirect:/login";
        }
        
        User user = getLoggedInUser(session);
        voucher.setSupplierId(user.getUserId());
        
        promotionVoucherRepository.save(voucher);
        redirectAttributes.addFlashAttribute("message", "Voucher created successfully!");
        
        return "redirect:/supplier/vouchers";
    }
    
    @GetMapping("/vouchers/{id}/edit")
    public String editVoucherForm(@PathVariable Integer id,
                                 Model model,
                                 HttpSession session) {
        if (!isSupplierLoggedIn(session)) {
            return "redirect:/login";
        }
        
        User user = getLoggedInUser(session);
        Optional<PromotionVoucher> voucherOpt = promotionVoucherRepository.findById(id);
        
        if (voucherOpt.isEmpty() || !voucherOpt.get().getSupplierId().equals(user.getUserId())) {
            return "redirect:/supplier/vouchers";
        }
        
        model.addAttribute("voucher", voucherOpt.get());
        model.addAttribute("isEdit", true);
        return "supplier/manage-voucher/add-voucher";
    }
    
    @PostMapping("/vouchers/{id}/edit")
    public String editVoucher(@PathVariable Integer id,
                             @ModelAttribute PromotionVoucher voucherDetails,
                             RedirectAttributes redirectAttributes,
                             HttpSession session) {
        if (!isSupplierLoggedIn(session)) {
            return "redirect:/login";
        }
        
        User user = getLoggedInUser(session);
        Optional<PromotionVoucher> voucherOpt = promotionVoucherRepository.findById(id);
        
        if (voucherOpt.isEmpty() || !voucherOpt.get().getSupplierId().equals(user.getUserId())) {
            return "redirect:/supplier/vouchers";
        }
        
        PromotionVoucher voucher = voucherOpt.get();
        voucher.setVoucherCode(voucherDetails.getVoucherCode());
        voucher.setDiscountType(voucherDetails.getDiscountType());
        voucher.setDiscountValue(voucherDetails.getDiscountValue());
        voucher.setMinPurchaseAmount(voucherDetails.getMinPurchaseAmount());
        voucher.setStartDate(voucherDetails.getStartDate());
        voucher.setEndDate(voucherDetails.getEndDate());
        voucher.setMaxUsage(voucherDetails.getMaxUsage());
        voucher.setIsActive(voucherDetails.getIsActive());
        
        promotionVoucherRepository.save(voucher);
        redirectAttributes.addFlashAttribute("message", "Voucher updated successfully!");
        
        return "redirect:/supplier/vouchers";
    }
    
    @GetMapping("/vouchers/{id}/products")
    public String assignProductsForm(@PathVariable Integer id,
                                    Model model,
                                    HttpSession session) {
        if (!isSupplierLoggedIn(session)) {
            return "redirect:/login";
        }
        
        User user = getLoggedInUser(session);
        Optional<PromotionVoucher> voucherOpt = promotionVoucherRepository.findById(id);
        
        if (voucherOpt.isEmpty() || !voucherOpt.get().getSupplierId().equals(user.getUserId())) {
            return "redirect:/supplier/vouchers";
        }
        
        PromotionVoucher voucher = voucherOpt.get();
        List<Product> supplierProducts = productRepository.findByUserId(user.getUserId());
        List<VoucherProduct> assignedProducts = voucherProductRepository.findByPromotionVoucher_VoucherId(id);
        
        // Mark which products are assigned
        Set<String> assignedProductIds = assignedProducts.stream()
            .map(VoucherProduct::getProductId)
            .collect(Collectors.toSet());
        
        model.addAttribute("voucher", voucher);
        model.addAttribute("products", supplierProducts);
        model.addAttribute("assignedProductIds", assignedProductIds);
        
        return "supplier/manage-voucher/vouchers-products";
    }
    
    @PostMapping("/vouchers/{id}/products/save")
    public String saveProductAssignments(@PathVariable Integer id,
                                        @RequestParam(value = "productIds", required = false) List<String> productIds,
                                        RedirectAttributes redirectAttributes,
                                        HttpSession session) {
        if (!isSupplierLoggedIn(session)) {
            return "redirect:/login";
        }
        
        User user = getLoggedInUser(session);
        Optional<PromotionVoucher> voucherOpt = promotionVoucherRepository.findById(id);
        
        if (voucherOpt.isEmpty() || !voucherOpt.get().getSupplierId().equals(user.getUserId())) {
            return "redirect:/supplier/vouchers";
        }
        
        // Clear existing assignments
        List<VoucherProduct> existingProducts = voucherProductRepository.findByPromotionVoucher_VoucherId(id);
        voucherProductRepository.deleteAll(existingProducts);
        
        // Add new assignments
        if (productIds != null && !productIds.isEmpty()) {
            for (String productId : productIds) {
                VoucherProduct vp = new VoucherProduct();
                vp.setPromotionVoucher(voucherOpt.get());
                vp.setProductId(productId);
                voucherProductRepository.save(vp);
            }
        }
        
        redirectAttributes.addFlashAttribute("message", "Product assignments saved successfully!");
        return "redirect:/supplier/vouchers";
    }
    
    @PostMapping("/vouchers/{id}/delete")
    public String deleteVoucher(@PathVariable Integer id,
                               RedirectAttributes redirectAttributes,
                               HttpSession session) {
        if (!isSupplierLoggedIn(session)) {
            return "redirect:/login";
        }
        
        User user = getLoggedInUser(session);
        Optional<PromotionVoucher> voucherOpt = promotionVoucherRepository.findById(id);
        
        if (voucherOpt.isEmpty() || !voucherOpt.get().getSupplierId().equals(user.getUserId())) {
            return "redirect:/supplier/vouchers";
        }
        
        // Delete associated product assignments
        List<VoucherProduct> products = voucherProductRepository.findByPromotionVoucher_VoucherId(id);
        voucherProductRepository.deleteAll(products);
        
        // Delete voucher
        promotionVoucherRepository.delete(voucherOpt.get());
        redirectAttributes.addFlashAttribute("message", "Voucher deleted successfully!");
        
        return "redirect:/supplier/vouchers";
    }
}