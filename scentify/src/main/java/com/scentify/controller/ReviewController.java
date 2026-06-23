package com.scentify.controller;

import com.scentify.model.Review;
import com.scentify.model.ReviewReply;
import com.scentify.model.Customer;
import com.scentify.model.Product;
import com.scentify.model.Supplier;
import com.scentify.model.User;
import com.scentify.model.Order;
import com.scentify.repository.ReviewRepository;
import com.scentify.repository.ReviewReplyRepository;
import com.scentify.repository.CustomerRepository;
import com.scentify.repository.ProductRepository;
import com.scentify.repository.SupplierRepository;
import com.scentify.repository.UserRepository;
import com.scentify.repository.OrderRepository;
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
@RequestMapping("/review")
public class ReviewController {
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private ReviewReplyRepository reviewReplyRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private SupplierRepository supplierRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    // ========== SESSION VALIDATION HELPER ==========
    private boolean isCustomerLoggedIn(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        return user != null && "customer".equalsIgnoreCase(user.getRole());
    }

    private boolean isSupplierLoggedIn(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        return user != null && "supplier".equalsIgnoreCase(user.getRole());
    }

    private User getLoggedInUser(HttpSession session) {
        return (User) session.getAttribute("loggedInUser");
    }

    // Check if customer has membership
    private boolean hasCustomerMembership(Customer customer) {
        return customer.getIsMember() != null && customer.getIsMember();
    }

    /**
     * Check if customer has purchased this product (supports multiple items per order)
     */
    private boolean hasCustomerPurchasedProduct(Customer customer, String productId) {
        List<Order> orders = orderRepository.findByCustomer_CustomerId((long) customer.getCustomerId());
        
        return orders.stream()
            .filter(order -> "PAID".equals(order.getPaymentStatus()) && 
                            ("CONFIRMED".equals(order.getOrderStatus()) || 
                             "DELIVERED".equals(order.getOrderStatus())))
            .flatMap(order -> order.getOrderItems().stream())
            .anyMatch(orderItem -> productId.equals(orderItem.getProduct().getProductId()));
    }

    /**
     * Check if customer can write review
     */
    private boolean canCustomerWriteReview(Customer customer, String productId) {
        if (!hasCustomerMembership(customer)) {
            return false;
        }
        if (!hasCustomerPurchasedProduct(customer, productId)) {
            return false;
        }
        Optional<Review> existingReview = reviewRepository.findByCustomerAndProduct_ProductId(customer, productId);
        return existingReview.isEmpty();
    }

    /**
     * Get reason why customer can't write review
     */
    private String getReviewBlockReason(Customer customer, String productId) {
        if (!hasCustomerMembership(customer)) {
            return "You need to be a member to write reviews. Please apply for membership.";
        }
        if (!hasCustomerPurchasedProduct(customer, productId)) {
            return "You can only review products you've purchased and received.";
        }
        Optional<Review> existingReview = reviewRepository.findByCustomerAndProduct_ProductId(customer, productId);
        if (existingReview.isPresent()) {
            return "You have already reviewed this product.";
        }
        return null;
    }

    // ========== SUBMIT REVIEW ==========
    @PostMapping("/submit")
    public String submitReview(
            @RequestParam String productId,
            @RequestParam Integer rating,
            @RequestParam String reviewText,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        if (!isCustomerLoggedIn(session)) {
            return "redirect:/login";
        }
        
        User user = getLoggedInUser(session);
        
        Optional<Product> productOpt = productRepository.findById(productId);
        Optional<Customer> customerOpt = customerRepository.findByUser(user);
        
        if (productOpt.isEmpty() || customerOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Product or customer not found");
            return "redirect:/customer/product/" + productId;
        }
        
        Product product = productOpt.get();
        Customer customer = customerOpt.get();
        
        if (!hasCustomerMembership(customer)) {
            redirectAttributes.addFlashAttribute("error", "Only members can write reviews. Please upgrade your membership.");
            return "redirect:/customer/product/" + productId;
        }
        
        if (!hasCustomerPurchasedProduct(customer, productId)) {
            redirectAttributes.addFlashAttribute("error", "You can only review products you've purchased and received.");
            return "redirect:/customer/product/" + productId;
        }
        
        Optional<Review> existingReview = reviewRepository.findByCustomerAndProduct_ProductId(customer, productId);
        if (existingReview.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "You have already reviewed this product.");
            return "redirect:/customer/product/" + productId;
        }
        
        if (reviewText == null || reviewText.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Review cannot be empty");
            return "redirect:/customer/product/" + productId;
        }
        
        if (rating == null || rating < 1 || rating > 5) {
            redirectAttributes.addFlashAttribute("error", "Please select a valid rating (1-5 stars)");
            return "redirect:/customer/product/" + productId;
        }
        
        Review review = new Review();
        review.setProduct(product);
        review.setCustomer(customer);
        review.setRating(rating);
        review.setReviewText(reviewText);
        review.setReviewStatus("APPROVED");
        review.setHelpfulCount(0);
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        
        reviewRepository.save(review);
        
        redirectAttributes.addFlashAttribute("success", "Review posted successfully!");
        return "redirect:/customer/product/" + productId;
    }

    // ========== SUPPLIER REPLY TO REVIEW ==========
    @PostMapping("/reply")
    public String replyToReview(
            @RequestParam Long reviewId,
            @RequestParam String replyText,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        if (!isSupplierLoggedIn(session)) {
            return "redirect:/login";
        }
        
        User user = getLoggedInUser(session);
        
        Optional<Supplier> supplierOpt = supplierRepository.findByUser_UserId(user.getUserId());
        Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
        
        if (supplierOpt.isEmpty() || reviewOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Supplier or review not found");
            return "redirect:/review/supplier/my-reviews";
        }
        
        Supplier supplier = supplierOpt.get();
        Review review = reviewOpt.get();
        
        if (!review.getProduct().getUserId().equals(user.getUserId())) {
            redirectAttributes.addFlashAttribute("error", "You can only reply to reviews of your own products");
            return "redirect:/review/supplier/my-reviews";
        }
        
        if (replyText == null || replyText.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Reply cannot be empty");
            return "redirect:/review/supplier/my-reviews";
        }
        
        Optional<ReviewReply> existingReply = reviewReplyRepository.findByReview_ReviewId(reviewId);
        ReviewReply reply;
        
        if (existingReply.isPresent()) {
            reply = existingReply.get();
            reply.setReplyText(replyText);
            reply.setUpdatedAt(LocalDateTime.now());
        } else {
            reply = new ReviewReply();
            reply.setReview(review);
            reply.setSupplier(supplier);
            reply.setReplyText(replyText);
            reply.setCreatedAt(LocalDateTime.now());
            reply.setUpdatedAt(LocalDateTime.now());
        }
        
        reviewReplyRepository.save(reply);
        
        redirectAttributes.addFlashAttribute("success", "Reply posted successfully!");
        return "redirect:/review/supplier/my-reviews";
    }

    // ========== VIEW REVIEW FORM ==========
    @GetMapping("/write/{productId}")
    public String showReviewForm(@PathVariable String productId, 
                                 HttpSession session, 
                                 Model model,
                                 RedirectAttributes redirect) {
        
        if (!isCustomerLoggedIn(session)) {
            return "redirect:/login";
        }
        
        User user = getLoggedInUser(session);
        
        Optional<Customer> customerOpt = customerRepository.findByUser(user);
        Optional<Product> productOpt = productRepository.findById(productId);
        
        if (customerOpt.isEmpty() || productOpt.isEmpty()) {
            redirect.addFlashAttribute("error", "Customer or product not found");
            return "redirect:/customer/dashboard";
        }
        
        Customer customer = customerOpt.get();
        Product product = productOpt.get();
        
        if (!"approved".equalsIgnoreCase(product.getApprovalStatus())) {
            redirect.addFlashAttribute("error", "This product is not available for review");
            return "redirect:/customer/dashboard";
        }
        
        String blockReason = getReviewBlockReason(customer, productId);
        if (blockReason != null) {
            redirect.addFlashAttribute("error", blockReason);
            return "redirect:/customer/product/" + productId;
        }
        
        model.addAttribute("product", product);
        model.addAttribute("customer", customer);
        model.addAttribute("user", user);
        
        return "customer/write-review";
    }

    // ========== ✅ FIXED: SUPPLIER DASHBOARD ==========
    @GetMapping("/supplier/my-reviews")
    public String getSupplierReviews(HttpSession session, Model model) {
        if (!isSupplierLoggedIn(session)) {
            return "redirect:/login";
        }
        
        User user = getLoggedInUser(session);
        
        Optional<Supplier> supplierOpt = supplierRepository.findByUser_UserId(user.getUserId());
        if (supplierOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        Supplier supplier = supplierOpt.get();
        
        // ✅ FIXED: Convert Integer to Long
        Long userId = user.getUserId().longValue();
        List<Review> allReviews = reviewRepository.findBySupplierUserId(userId);
        
        // ✅ FIXED: Get all replies by this supplier (using reviewReplyRepository)
        List<ReviewReply> replies = reviewReplyRepository.findBySupplier(supplier);
        
        model.addAttribute("reviews", allReviews);
        model.addAttribute("replies", replies);
        model.addAttribute("supplier", supplier);
        
        return "supplier/reviews-dashboard";
    }

    // ========== HELPFUL COUNTER ==========
    @PostMapping("/helpful/{reviewId}")
    @ResponseBody
    public String markReviewHelpful(@PathVariable Long reviewId, HttpSession session) {
        if (!isCustomerLoggedIn(session)) {
            return "error: Please login to mark reviews as helpful";
        }
        
        Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
        if (reviewOpt.isEmpty()) {
            return "error: Review not found";
        }
        
        Review review = reviewOpt.get();
        review.setHelpfulCount(review.getHelpfulCount() + 1);
        reviewRepository.save(review);
        
        return "success: " + review.getHelpfulCount();
    }

    // ========== API: CHECK IF CUSTOMER CAN REVIEW ==========
    @GetMapping("/can-review/{productId}")
    @ResponseBody
    public boolean canReviewProduct(@PathVariable String productId, HttpSession session) {
        if (!isCustomerLoggedIn(session)) {
            return false;
        }
        
        User user = getLoggedInUser(session);
        Optional<Customer> customerOpt = customerRepository.findByUser(user);
        
        if (customerOpt.isEmpty()) {
            return false;
        }
        
        Customer customer = customerOpt.get();
        return canCustomerWriteReview(customer, productId);
    }

    // ========== API: GET REVIEW BLOCK REASON ==========
    @GetMapping("/review-block-reason/{productId}")
    @ResponseBody
    public String getReviewBlockReason(@PathVariable String productId, HttpSession session) {
        if (!isCustomerLoggedIn(session)) {
            return "Please login to write a review";
        }
        
        User user = getLoggedInUser(session);
        Optional<Customer> customerOpt = customerRepository.findByUser(user);
        
        if (customerOpt.isEmpty()) {
            return "Customer not found";
        }
        
        Customer customer = customerOpt.get();
        String reason = getReviewBlockReason(customer, productId);
        return reason != null ? reason : "You can write a review!";
    }
}