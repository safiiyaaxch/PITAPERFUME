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

    // Check if customer has purchased this product
    private boolean hasCustomerPurchasedProduct(Customer customer, String productId) {
        List<Order> orders = orderRepository.findByCustomer_CustomerId((long) customer.getCustomerId());
        return orders.stream()
            .anyMatch(order -> productId.equals(order.getProduct().getProductId()) && 
                             ("DELIVERED".equals(order.getOrderStatus()) || "SHIPPED".equals(order.getOrderStatus())));
    }

    // Check if customer can write review
    private boolean canCustomerWriteReview(Customer customer, String productId) {
        return hasCustomerMembership(customer) && hasCustomerPurchasedProduct(customer, productId);
    }

    // Submit a new review
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
        
        Optional<Product> product = productRepository.findById(productId);
        Optional<Customer> customer = customerRepository.findById(user.getUserId());
        
        if (product.isEmpty() || customer.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Product or customer not found");
            return "redirect:/customer/product/" + productId;
        }
        
        // Check membership
        if (!hasCustomerMembership(customer.get())) {
            redirectAttributes.addFlashAttribute("error", "Only members can write reviews. Please upgrade your membership.");
            return "redirect:/customer/product/" + productId;
        }
        
        // Check if customer purchased this product
        if (!hasCustomerPurchasedProduct(customer.get(), productId)) {
            redirectAttributes.addFlashAttribute("error", "You can only review products you've purchased.");
            return "redirect:/customer/product/" + productId;
        }
        
        // Validate review text
        if (reviewText == null || reviewText.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Review cannot be empty");
            return "redirect:/customer/product/" + productId;
        }
        
        // Validate rating
        if (rating == null || rating < 1 || rating > 5) {
            redirectAttributes.addFlashAttribute("error", "Please select a valid rating (1-5 stars)");
            return "redirect:/customer/product/" + productId;
        }
        
        // Create new review
        Review review = new Review();
        review.setProduct(product.get());
        review.setCustomer(customer.get());
        review.setRating(rating);
        review.setReviewText(reviewText);
        review.setReviewStatus("APPROVED"); // Auto-approve reviews
        review.setHelpfulCount(0);
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        
        reviewRepository.save(review);
        
        redirectAttributes.addFlashAttribute("success", "Review posted successfully!");
        return "redirect:/customer/product/" + productId;
    }

    // Supplier reply to a review
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
        
        Optional<Supplier> supplier = supplierRepository.findByUser_UserId(user.getUserId());
        Optional<Review> review = reviewRepository.findById(reviewId);
        
        if (supplier.isEmpty() || review.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Supplier or review not found");
            return "redirect:/review/supplier/my-reviews";
        }
        
        // Check if supplier owns the product being reviewed
        if (!review.get().getProduct().getUserId().equals(user.getUserId())) {
            redirectAttributes.addFlashAttribute("error", "You can only reply to reviews of your own products");
            return "redirect:/review/supplier/my-reviews";
        }
        
        // Validate reply text
        if (replyText == null || replyText.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Reply cannot be empty");
            return "redirect:/review/supplier/my-reviews";
        }
        
        // Create new reply
        ReviewReply reply = new ReviewReply();
        reply.setReview(review.get());
        reply.setSupplier(supplier.get());
        reply.setReplyText(replyText);
        reply.setCreatedAt(LocalDateTime.now());
        reply.setUpdatedAt(LocalDateTime.now());
        
        // Delete existing reply if any
        Optional<ReviewReply> existingReply = reviewReplyRepository.findByReview_ReviewId(reviewId);
        if (existingReply.isPresent()) {
            reviewReplyRepository.delete(existingReply.get());
        }
        
        reviewReplyRepository.save(reply);
        
        redirectAttributes.addFlashAttribute("success", "Reply posted successfully!");
        return "redirect:/review/supplier/my-reviews";
    }

    // Supplier dashboard - view reviews for their products
    @GetMapping("/supplier/my-reviews")
    public String getSupplierReviews(HttpSession session, Model model) {
        if (!isSupplierLoggedIn(session)) {
            return "redirect:/login";
        }
        
        User user = getLoggedInUser(session);
        
        Optional<Supplier> supplier = supplierRepository.findByUser_UserId(user.getUserId());
        if (supplier.isEmpty()) {
            return "redirect:/login";
        }
        
        // Get all reviews for products owned by this supplier (userId matches)
        List<Review> allReviews = reviewRepository.findBySupplierUserId(user.getUserId());
        
        model.addAttribute("reviews", allReviews);
        model.addAttribute("supplier", supplier.get());
        
        return "supplier/reviews-dashboard";
    }
}
