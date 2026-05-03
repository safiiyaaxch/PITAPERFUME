package com.scentify.controller;

import com.scentify.model.Product;
import com.scentify.model.User;
import com.scentify.model.Supplier;
import com.scentify.model.Customer;
import com.scentify.model.MembershipApplication;
import com.scentify.repository.ProductRepository;
import com.scentify.repository.SupplierRepository;
import com.scentify.repository.UserRepository;
import com.scentify.repository.CustomerRepository;
import com.scentify.repository.MembershipApplicationRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private MembershipApplicationRepository membershipApplicationRepository;
    
    // ========== SESSION VALIDATION HELPER ==========
    private boolean isManagerLoggedIn(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        return user != null && "system_manager".equalsIgnoreCase(user.getRole());
    }
    
    private User getLoggedInUser(HttpSession session) {
        return (User) session.getAttribute("loggedInUser");
    }
    
    // ========== MAIN DASHBOARD ==========
        @GetMapping("/dashboard")
        public String mainDashboard(HttpSession session, Model model) {
            if (!isManagerLoggedIn(session)) {
                return "redirect:/login";
            }
            
            // Get lists for dashboard
            List<Supplier> pendingSuppliers = supplierRepository.findByApprovalStatus("pending");
            List<Supplier> approvedSuppliers = supplierRepository.findByApprovalStatus("approved");
            List<Supplier> rejectedSuppliers = supplierRepository.findByApprovalStatus("rejected");
            List<Product> pendingProducts = productRepository.findByApprovalStatus("pending");
            
            model.addAttribute("pendingSuppliers", pendingSuppliers.size());
            model.addAttribute("approvedSuppliers", approvedSuppliers.size());
            model.addAttribute("rejectedSuppliers", rejectedSuppliers.size());
            model.addAttribute("pendingProducts", pendingProducts.size());
            
            // Get pending membership applications
            List<MembershipApplication> pendingMemberships = membershipApplicationRepository.findByStatusOrderByAppliedDateDesc("PENDING");
            model.addAttribute("pendingMemberships", pendingMemberships.size());
            model.addAttribute("user", getLoggedInUser(session));
            
            return "manager/dashboard";
        }

        // ========== MANAGE SUPPLIERS SECTION ==========
        @GetMapping("/suppliers")
        public String manageSuppliersPage(HttpSession session, Model model) {
            if (!isManagerLoggedIn(session)) {
                return "redirect:/login";
            }
            
            List<Supplier> pendingSuppliers = supplierRepository.findByApprovalStatus("pending");
            List<Supplier> approvedSuppliers = supplierRepository.findByApprovalStatus("approved");
            List<Supplier> rejectedSuppliers = supplierRepository.findByApprovalStatus("rejected");
            
            model.addAttribute("pendingSuppliers", pendingSuppliers);
            model.addAttribute("approvedSuppliers", approvedSuppliers);
            model.addAttribute("rejectedSuppliers", rejectedSuppliers);
            model.addAttribute("user", getLoggedInUser(session));
            
            return "manager/manage-supplier/supplier-dashboard";
        }

        // ========== VIEW SUPPLIER DETAILS FOR APPROVAL ==========
        @GetMapping("/suppliers/{id}")
        public String viewSupplierForApproval(@PathVariable Integer id, 
                                            HttpSession session, 
                                            Model model) {
            if (!isManagerLoggedIn(session)) {
                return "redirect:/login";
            }
            
            Optional<Supplier> supplierOpt = supplierRepository.findById(id);
            if (supplierOpt.isEmpty()) {
                return "redirect:/manager/suppliers";
            }
            
            model.addAttribute("supplier", supplierOpt.get());
            model.addAttribute("user", getLoggedInUser(session));
            return "manager/manage-supplier/supplier-approval";
        }

        // ========== DOWNLOAD/VIEW PDF ==========
        @GetMapping("/suppliers/{id}/pdf")
        public ResponseEntity<?> downloadSupplierPDF(@PathVariable Integer id, 
                                                    HttpSession session) {
            if (!isManagerLoggedIn(session)) {
                return ResponseEntity.status(401).build();
            }
            
            Optional<Supplier> supplierOpt = supplierRepository.findById(id);
            if (supplierOpt.isEmpty() || supplierOpt.get().getBusinessRegistration() == null) {
                return ResponseEntity.notFound().build();
            }
            
            try {
                String fileName = supplierOpt.get().getBusinessRegistration();
                Path filePath = Paths.get("uploads/business-docs/" + fileName);
                byte[] fileContent = Files.readAllBytes(filePath);
                
                return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(fileContent);
            } catch (Exception e) {
                System.out.println("Error downloading PDF: " + e.getMessage());
                return ResponseEntity.notFound().build();
            }
        }

        // ========== APPROVE SUPPLIER ==========
        @PostMapping("/suppliers/{id}/approve")
        public String approveSupplier(@PathVariable Integer id,
                                    @RequestParam(required = false) String approvalNotes,
                                    HttpSession session,
                                    RedirectAttributes redirect) {
            if (!isManagerLoggedIn(session)) {
                return "redirect:/login";
            }
            
            Optional<Supplier> supplierOpt = supplierRepository.findById(id);
            if (supplierOpt.isEmpty()) {
                redirect.addFlashAttribute("error", "Supplier not found");
                return "redirect:/manager/suppliers";
            }
            
            try {
                Supplier supplier = supplierOpt.get();
                supplier.setApprovalStatus("approved");
                supplier.setApprovalDate(LocalDateTime.now());
                if (approvalNotes != null && !approvalNotes.trim().isEmpty()) {
                    supplier.setApprovalNotes(approvalNotes);
                }
                supplier.setUpdatedAt(LocalDateTime.now());
                supplierRepository.save(supplier);
                
                redirect.addFlashAttribute("success", "✓ Supplier '" + supplier.getBrandName() + "' has been approved!");
                System.out.println("Supplier approved: " + id + " by manager: " + getLoggedInUser(session).getUserId());
            } catch (Exception e) {
                System.out.println("Error approving supplier: " + e.getMessage());
                redirect.addFlashAttribute("error", "Failed to approve supplier");
            }
            
            return "redirect:/manager/suppliers";
        }

        // ========== REJECT SUPPLIER ==========
        @PostMapping("/suppliers/{id}/reject")
        public String rejectSupplier(@PathVariable Integer id,
                                    @RequestParam(required = false) String approvalNotes,
                                    HttpSession session,
                                    RedirectAttributes redirect) {
            if (!isManagerLoggedIn(session)) {
                return "redirect:/login";
            }
            
            Optional<Supplier> supplierOpt = supplierRepository.findById(id);
            if (supplierOpt.isEmpty()) {
                redirect.addFlashAttribute("error", "Supplier not found");
                return "redirect:/manager/suppliers";
            }
            
            try {
                Supplier supplier = supplierOpt.get();
                supplier.setApprovalStatus("rejected");
                supplier.setApprovalDate(LocalDateTime.now());
                if (approvalNotes != null && !approvalNotes.trim().isEmpty()) {
                    supplier.setApprovalNotes(approvalNotes);
                }
                supplier.setUpdatedAt(LocalDateTime.now());
                supplierRepository.save(supplier);
                
                redirect.addFlashAttribute("warning", "✗ Supplier '" + supplier.getBrandName() + "' has been rejected!");
                System.out.println("Supplier rejected: " + id + " by manager: " + getLoggedInUser(session).getUserId());
            } catch (Exception e) {
                System.out.println("Error rejecting supplier: " + e.getMessage());
                redirect.addFlashAttribute("error", "Failed to reject supplier");
            }
            
            return "redirect:/manager/suppliers";
        }

        // ========== MANAGE PRODUCTS SECTION ==========
        @GetMapping("/products")
        public String manageProductsPage(HttpSession session, Model model) {
            if (!isManagerLoggedIn(session)) {
                return "redirect:/login";
            }
            
            List<Product> pendingProducts = productRepository.findByApprovalStatus("pending");
            List<Product> approvedProducts = productRepository.findByApprovalStatus("approved");
            List<Product> rejectedProducts = productRepository.findByApprovalStatus("rejected");
            
            // Add supplier name to each product
            for (Product product : pendingProducts) {
                Optional<User> user = userRepository.findById(product.getUserId());
                product.setSupplierName(user.isPresent() ? user.get().getUsername() : "Unknown");
            }
            for (Product product : approvedProducts) {
                Optional<User> user = userRepository.findById(product.getUserId());
                product.setSupplierName(user.isPresent() ? user.get().getUsername() : "Unknown");
            }
            for (Product product : rejectedProducts) {
                Optional<User> user = userRepository.findById(product.getUserId());
                product.setSupplierName(user.isPresent() ? user.get().getUsername() : "Unknown");
            }
            
            model.addAttribute("pendingProducts", pendingProducts);
            model.addAttribute("approvedProducts", approvedProducts);
            model.addAttribute("rejectedProducts", rejectedProducts);
            model.addAttribute("user", getLoggedInUser(session));
            
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

    // ========== MEMBERSHIP APPLICATION MANAGEMENT ==========
    @GetMapping("/membership-applications")
    public String viewMembershipApplications(HttpSession session, Model model) {
        // Check if user is manager
        if (!isManagerLoggedIn(session)) {
            return "redirect:/login";
        }

        System.out.println("📋 Manager viewing membership applications");

        // Get all pending applications sorted by newest first
        List<MembershipApplication> pendingApplications = membershipApplicationRepository
                .findByStatusOrderByAppliedDateDesc("PENDING");

        // Get all approved applications for history
        List<MembershipApplication> approvedApplications = membershipApplicationRepository
                .findByStatusOrderByAppliedDateDesc("APPROVED");

        // Get all rejected applications for history
        List<MembershipApplication> rejectedApplications = membershipApplicationRepository
                .findByStatusOrderByAppliedDateDesc("REJECTED");

        model.addAttribute("pendingApplications", pendingApplications);
        model.addAttribute("approvedApplications", approvedApplications);
        model.addAttribute("rejectedApplications", rejectedApplications);
        model.addAttribute("pendingCount", pendingApplications.size());
        model.addAttribute("approvedCount", approvedApplications.size());
        model.addAttribute("rejectedCount", rejectedApplications.size());

        System.out.println("✅ Found " + pendingApplications.size() + " pending applications");
        return "manager/membership-applications";
    }

    @PostMapping("/membership-applications/{applicationId}/approve")
    public String approveMembershipApplication(
            @PathVariable Integer applicationId,
            @RequestParam(required = false) String notes,
            HttpSession session,
            RedirectAttributes redirect) {

        if (!isManagerLoggedIn(session)) {
            return "redirect:/login";
        }

        Optional<MembershipApplication> applicationOpt = membershipApplicationRepository.findById(applicationId);

        if (applicationOpt.isEmpty()) {
            redirect.addFlashAttribute("error", "Application not found");
            return "redirect:/manager/membership-applications";
        }

        MembershipApplication application = applicationOpt.get();

        // Check if application is still pending
        if (!"PENDING".equals(application.getStatus())) {
            redirect.addFlashAttribute("error", "This application has already been processed");
            return "redirect:/manager/membership-applications";
        }

        try {
            // Update application status
            application.setStatus("APPROVED");
            application.setApprovedDate(LocalDateTime.now());
            if (notes != null && !notes.isBlank()) {
                application.setNotes(notes);
            }
            membershipApplicationRepository.save(application);

            // Update customer membership status
            Customer customer = application.getCustomer();
            customer.setIsMember(true);
            customerRepository.save(customer);

            System.out.println("✅ Membership application approved for customer: " + customer.getFullname());
            redirect.addFlashAttribute("success", "Membership application approved successfully!");

        } catch (Exception e) {
            System.out.println("❌ Error approving membership application: " + e.getMessage());
            e.printStackTrace();
            redirect.addFlashAttribute("error", "Failed to approve application: " + e.getMessage());
        }

        return "redirect:/manager/membership-applications";
    }

    @PostMapping("/membership-applications/{applicationId}/reject")
    public String rejectMembershipApplication(
            @PathVariable Integer applicationId,
            @RequestParam(required = false) String rejectionReason,
            HttpSession session,
            RedirectAttributes redirect) {

        if (!isManagerLoggedIn(session)) {
            return "redirect:/login";
        }

        Optional<MembershipApplication> applicationOpt = membershipApplicationRepository.findById(applicationId);

        if (applicationOpt.isEmpty()) {
            redirect.addFlashAttribute("error", "Application not found");
            return "redirect:/manager/membership-applications";
        }

        MembershipApplication application = applicationOpt.get();

        // Check if application is still pending
        if (!"PENDING".equals(application.getStatus())) {
            redirect.addFlashAttribute("error", "This application has already been processed");
            return "redirect:/manager/membership-applications";
        }

        if (rejectionReason == null || rejectionReason.isBlank()) {
            redirect.addFlashAttribute("error", "Please provide a rejection reason");
            return "redirect:/manager/membership-applications";
        }

        try {
            // Update application status
            application.setStatus("REJECTED");
            application.setApprovedDate(LocalDateTime.now());
            application.setRejectionReason(rejectionReason);
            membershipApplicationRepository.save(application);

            Customer customer = application.getCustomer();
            System.out.println("❌ Membership application rejected for customer: " + customer.getFullname());
            redirect.addFlashAttribute("success", "Membership application rejected successfully!");

        } catch (Exception e) {
            System.out.println("❌ Error rejecting membership application: " + e.getMessage());
            e.printStackTrace();
            redirect.addFlashAttribute("error", "Failed to reject application: " + e.getMessage());
        }

        return "redirect:/manager/membership-applications";
    }

    @GetMapping("/membership-applications/{applicationId}/details")
    public String viewApplicationDetails(
            @PathVariable Integer applicationId,
            HttpSession session,
            Model model) {

        if (!isManagerLoggedIn(session)) {
            return "redirect:/login";
        }

        Optional<MembershipApplication> applicationOpt = membershipApplicationRepository.findById(applicationId);

        if (applicationOpt.isEmpty()) {
            return "redirect:/manager/membership-applications";
        }

        MembershipApplication application = applicationOpt.get();
        Customer customer = application.getCustomer();

        model.addAttribute("application", application);
        model.addAttribute("customer", customer);

        return "manager/membership-application-details";
    }
}
