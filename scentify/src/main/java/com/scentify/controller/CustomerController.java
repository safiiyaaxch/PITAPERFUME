package com.scentify.controller;

import com.scentify.model.Customer;
import com.scentify.model.Product;
import com.scentify.model.PromotionVoucher;
import com.scentify.model.User;
import com.scentify.model.ShoppingCart;
import com.scentify.model.CartItem;
import com.scentify.model.Order;
import com.scentify.model.OrderItem;
import com.scentify.model.Payment;
import com.scentify.model.OrderVoucher;
import com.scentify.model.MembershipApplication;
import com.scentify.model.Review;

import com.scentify.repository.ProductRepository;
import com.scentify.repository.PromotionVoucherRepository;
import com.scentify.repository.UserRepository;
import com.scentify.repository.CustomerRepository;
import com.scentify.repository.ShoppingCartRepository;
import com.scentify.repository.CartItemRepository;
import com.scentify.repository.OrderRepository;
import com.scentify.repository.OrderVoucherRepository;
import com.scentify.repository.MembershipApplicationRepository;
import com.scentify.repository.ReviewRepository;
import com.scentify.repository.OrderItemRepository;
import com.scentify.service.ToyyibPayService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

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

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderVoucherRepository orderVoucherRepository;
    
    @Autowired
    private MembershipApplicationRepository membershipApplicationRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;
    
    @Autowired
    private ToyyibPayService toyyibPayService;

    
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
                        @RequestParam(required = false) String category,
                        @RequestParam(required = false) Double minPrice,
                        @RequestParam(required = false) Double maxPrice,
                        @RequestParam(required = false) Double minRating) {
    
    System.out.println("========== CUSTOMER DASHBOARD ==========");
    User user = getLoggedInUser(session);
    
    if (!isCustomerLoggedIn(session)) {
        return "redirect:/login";
    }

    // CHECK FLASH ATTRIBUTES FIRST (from RedirectAttributes)
    Boolean paymentPopup = (Boolean) model.getAttribute("paymentPopup");
    
    // If not in flash, check session
    if (paymentPopup == null) {
        paymentPopup = (Boolean) session.getAttribute("paymentPopup");
        System.out.println("🔍 Checked session for paymentPopup: " + paymentPopup);
    } else {
        System.out.println("🔍 Found paymentPopup in flash attributes: " + paymentPopup);
    }
    
    if (paymentPopup != null && paymentPopup) {
        // Get values from flash attributes first, fallback to session
        Boolean paymentSuccess = (Boolean) model.getAttribute("paymentSuccess");
        if (paymentSuccess == null) {
            paymentSuccess = (Boolean) session.getAttribute("paymentSuccess");
        }
        
        Long paymentOrderId = (Long) model.getAttribute("paymentOrderId");
        if (paymentOrderId == null) {
            paymentOrderId = (Long) session.getAttribute("paymentOrderId");
        }
        
        Long paymentInvoiceId = (Long) model.getAttribute("paymentInvoiceId");
        if (paymentInvoiceId == null) {
            paymentInvoiceId = (Long) session.getAttribute("paymentInvoiceId");
        }
        
        String paymentMessage = (String) model.getAttribute("paymentMessage");
        if (paymentMessage == null) {
            paymentMessage = (String) session.getAttribute("paymentMessage");
        }
        
        model.addAttribute("paymentPopup", true);
        model.addAttribute("paymentSuccess", paymentSuccess);
        model.addAttribute("paymentOrderId", paymentOrderId);
        model.addAttribute("paymentInvoiceId", paymentInvoiceId);
        model.addAttribute("paymentMessage", paymentMessage);
        
        System.out.println("✅ Payment popup added to model:");
        System.out.println("   paymentSuccess: " + paymentSuccess);
        System.out.println("   paymentOrderId: " + paymentOrderId);
        System.out.println("   paymentMessage: " + paymentMessage);
    }
    
    // Get all approved products
    List<Product> approvedProducts = productRepository.findByApprovalStatus("approved");
    
    // Filter by category
    if (category != null && !category.isEmpty()) {
        approvedProducts = approvedProducts.stream()
                .filter(p -> p.getCategory() != null && p.getCategory().equalsIgnoreCase(category))
                .toList();
    }
    
    // Filter by price range
    if (minPrice != null) {
        approvedProducts = approvedProducts.stream()
                .filter(p -> p.getPrice() >= minPrice)
                .toList();
    }
    if (maxPrice != null) {
        approvedProducts = approvedProducts.stream()
                .filter(p -> p.getPrice() <= maxPrice)
                .toList();
    }
    
    // Filter by search term
    if (search != null && !search.isEmpty()) {
        String searchTerm = search.toLowerCase();
        approvedProducts = approvedProducts.stream()
                .filter(p -> p.getProductName().toLowerCase().contains(searchTerm) ||
                        (p.getDescription() != null && p.getDescription().toLowerCase().contains(searchTerm)))
                .toList();
    }
    
    // Calculate ratings and filter by minRating
    java.util.Map<String, Double> productRatings = new java.util.HashMap<>();
    List<Product> finalProducts = new java.util.ArrayList<>();
    
    for (Product product : approvedProducts) {
        List<Review> reviews = reviewRepository.findByProduct_ProductId(product.getProductId());
        double avgRating = 0.0;
        if (reviews != null && !reviews.isEmpty()) {
            avgRating = reviews.stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);
        }
        productRatings.put(product.getProductId(), avgRating);
        
        if (minRating == null || avgRating >= minRating) {
            finalProducts.add(product);
        }
    }
    
    // Get customer details
    Optional<Customer> customerOpt = customerRepository.findByUser(user);
    if (customerOpt.isPresent()) {
        model.addAttribute("customer", customerOpt.get());
        Optional<MembershipApplication> membershipApp = membershipApplicationRepository.findByCustomer(customerOpt.get());
        membershipApp.ifPresent(app -> model.addAttribute("membershipApplication", app));
    }
    
    model.addAttribute("user", user);
    model.addAttribute("approvedProducts", finalProducts);
    model.addAttribute("productRatings", productRatings);
    model.addAttribute("search", search);
    model.addAttribute("category", category);
    model.addAttribute("minPrice", minPrice);
    model.addAttribute("maxPrice", maxPrice);
    model.addAttribute("minRating", minRating);
    
    return "customer/dashboard";
}
   
    // ========== ABOUT US ==========
@GetMapping("/about")
public String aboutUs(HttpSession session, Model model) {
    if (!isCustomerLoggedIn(session)) {
        return "redirect:/login";
    }
    
    User user = getLoggedInUser(session);
    model.addAttribute("user", user);
    
    // Get customer details if available
    Optional<Customer> customerOpt = customerRepository.findByUser(user);
    customerOpt.ifPresent(customer -> model.addAttribute("customer", customer));
    
    return "customer/about";
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
    
    // Get approved reviews for this product
    List<Review> reviews = reviewRepository.findByProduct_ProductId(id);
    
    // Calculate review statistics
    if (reviews != null && !reviews.isEmpty()) {
        double averageRating = reviews.stream()
            .mapToDouble(Review::getRating)
            .average()
            .orElse(0.0);
        
        java.util.Map<Integer, Long> starDistribution = new java.util.LinkedHashMap<>();
        for (int i = 5; i >= 1; i--) {
            final int stars = i;
            long count = reviews.stream()
                .filter(r -> r.getRating() == stars)
                .count();
            starDistribution.put(i, count);
        }
        
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("starDistribution", starDistribution);
    }
    
    // Check if customer can write review
    User currentUser = getLoggedInUser(session);
    Optional<Customer> currentCustomer = customerRepository.findByUser(currentUser);
    
    boolean canWriteReview = false;
    String reviewBlockReason = null;
    
    if (currentCustomer.isPresent()) {
        Customer customer = currentCustomer.get();
        
        if (!customer.getIsMember()) {
            reviewBlockReason = "Upgrade to membership to write reviews";
            canWriteReview = false;
        } else {
            // ✅ Check if customer purchased AND received this product (DELIVERED)
            List<Order> orders = orderRepository.findByCustomer_CustomerId((long) customer.getCustomerId());
            
            boolean hasPurchasedAndDelivered = orders.stream()
                .filter(order -> "PAID".equals(order.getPaymentStatus()) && 
                                "DELIVERED".equals(order.getOrderStatus()))  // Must be delivered
                .flatMap(order -> order.getOrderItems().stream())
                .anyMatch(orderItem -> id.equals(orderItem.getProduct().getProductId()));
            
            if (!hasPurchasedAndDelivered) {
                reviewBlockReason = "You can only review products that have been delivered to you";
                canWriteReview = false;
            } else {
                canWriteReview = true;
            }
        }
    }
    
    model.addAttribute("product", product);
    model.addAttribute("user", getLoggedInUser(session));
    model.addAttribute("reviews", reviews);
    model.addAttribute("canWriteReview", canWriteReview);
    model.addAttribute("reviewBlockReason", reviewBlockReason);
    
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
                    .filter(p -> p.getCategory().equalsIgnoreCase(category))
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
   @GetMapping("/checkout-product")
public String checkoutProduct(@RequestParam String productId,
                              HttpSession session, 
                              Model model) {
    
    if (!isCustomerLoggedIn(session)) {
        return "redirect:/login";
    }
    
    User user = getLoggedInUser(session);
    
    Optional<Product> productOpt = productRepository.findById(productId);
    if (productOpt.isEmpty()) {
        return "redirect:/customer/dashboard?error=Product not found";
    }
    
    Product product = productOpt.get();
    
    Optional<Customer> customerOpt = customerRepository.findByUser(user);
    if (customerOpt.isEmpty()) {
        return "redirect:/login";
    }
    
    Customer customer = customerOpt.get();
    
    // Create temporary cart
    ShoppingCart tempCart = new ShoppingCart();
    CartItem tempItem = new CartItem();
    tempItem.setProduct(product);
    tempItem.setQuantity(1);
    tempCart.addCartItem(tempItem);
    
    
    session.setAttribute("directPurchaseCart", tempCart);
    session.setAttribute("isDirectPurchase", true);
    
    // Get available vouchers
    List<PromotionVoucher> availableVouchers = new ArrayList<>();
    if (customer.getIsMember() != null && customer.getIsMember()) {
        LocalDateTime now = LocalDateTime.now();
        availableVouchers = promotionVoucherRepository.findAll().stream()
                .filter(v -> v.getIsActive() != null && v.getIsActive())
                .filter(v -> v.getEndDate() == null || v.getEndDate().isAfter(now))
                .filter(v -> v.getStartDate() == null || v.getStartDate().isBefore(now))
                .filter(v -> v.getMaxUsage() == null || v.getCurrentUsage() < v.getMaxUsage())
                .filter(v -> v.getVoucherProducts() != null && v.getVoucherProducts().stream()
                        .anyMatch(vp -> vp.getProductId().equals(productId)))
                .toList();
    }
    
    model.addAttribute("product", product);
    model.addAttribute("customer", customer);
    model.addAttribute("user", user);
    model.addAttribute("cart", tempCart);
    model.addAttribute("availableVouchers", availableVouchers);
    model.addAttribute("isDirectPurchase", true);
    
    return "customer/checkout";
}

    // ========== SHOPPING CART ==========
    @PostMapping("/cart/add")
    public String addToCart(@RequestParam String productId,
                           @RequestParam(defaultValue = "1") Integer quantity,
                           HttpSession session,
                           RedirectAttributes redirect) {
        
        System.out.println("=== ADD TO CART REQUEST ===");
        System.out.println("ProductId received: [" + productId + "]");
        System.out.println("Quantity: " + quantity);
        
        if (!isCustomerLoggedIn(session)) {
            System.out.println("ERROR: Customer not logged in");
            return "redirect:/login";
        }

        try {
            User user = getLoggedInUser(session);
            System.out.println("User found: " + user.getUsername());
            
            Optional<Customer> customerOpt = customerRepository.findByUser(user);
            
            if (customerOpt.isEmpty()) {
                System.out.println("ERROR: Customer not found for user: " + user.getUsername());
                redirect.addFlashAttribute("error", "Customer not found");
                return "redirect:/customer/dashboard";
            }

            Customer customer = customerOpt.get();
            System.out.println("Customer found: " + customer.getFullname());
            
            // Get or create shopping cart
            Optional<ShoppingCart> cartOpt = shoppingCartRepository.findByCustomer(customer);
            ShoppingCart cart = cartOpt.orElseGet(() -> new ShoppingCart(customer));
            System.out.println("Cart ID: " + cart.getCartId());

            // Get product
            System.out.println("Looking for product with ID: [" + productId + "]");
            Optional<Product> productOpt = productRepository.findById(productId);
            
            if (productOpt.isEmpty()) {
                System.out.println("ERROR: Product not found with ID: " + productId);
                System.out.println("Available products in database:");
                productRepository.findAll().forEach(p -> {
                    System.out.println("  - ID: [" + p.getProductId() + "], Name: " + p.getProductName());
                });
                redirect.addFlashAttribute("error", "Product not found with ID: " + productId);
                return "redirect:/customer/dashboard";
            }

            Product product = productOpt.get();
            System.out.println("Product found: " + product.getProductName());
            
            // Check if product already in cart
            Optional<CartItem> existingItem = cartItemRepository
                    .findByCart_CartIdAndProduct_ProductId(cart.getCartId(), productId);
            
            if (existingItem.isPresent()) {
                // Update quantity
                CartItem item = existingItem.get();
                System.out.println("Item already in cart, updating quantity from " + item.getQuantity() + " to " + (item.getQuantity() + quantity));
                item.setQuantity(item.getQuantity() + quantity);
                item.setUpdatedAt(LocalDateTime.now());
                cartItemRepository.save(item);
            } else {
                // Add new item
                System.out.println("Adding new item to cart");
                CartItem newItem = new CartItem(cart, product, quantity);
                cart.addCartItem(newItem);
            }

            cart.setUpdatedAt(LocalDateTime.now());
            shoppingCartRepository.save(cart);
            System.out.println("Cart saved successfully");
            
            redirect.addFlashAttribute("success", "Product added to cart!");
            return "redirect:/customer/cart";
        } catch (Exception e) {
            System.out.println("ERROR adding to cart: " + e.getMessage());
            System.out.println("Exception type: " + e.getClass().getName());
            e.printStackTrace();
            redirect.addFlashAttribute("error", "Failed to add product to cart: " + e.getMessage());
            return "redirect:/customer/dashboard";
        }
    }

    @GetMapping("/cart")
    @Transactional
    public String viewCart(HttpSession session, Model model) {
        
        if (!isCustomerLoggedIn(session)) {
            return "redirect:/login";
        }

        try {
            User user = getLoggedInUser(session);
            Optional<Customer> customerOpt = customerRepository.findByUser(user);

            if (customerOpt.isEmpty()) {
                return "redirect:/customer/dashboard";
            }

            Customer customer = customerOpt.get();
            Optional<ShoppingCart> cartOpt = shoppingCartRepository.findByCustomer(customer);

            ShoppingCart cart;
            if (cartOpt.isEmpty()) {
                cart = new ShoppingCart(customer);
            } else {
                cart = cartOpt.get();
                
                // Clean up orphaned cart items (products that no longer exist)
                // Use a raw query to find items with missing products
                List<CartItem> allItems = new java.util.ArrayList<>();
                try {
                    allItems = cartItemRepository.findByCart_CartId(cart.getCartId());
                } catch (org.springframework.orm.jpa.JpaObjectRetrievalFailureException e) {
                    // This exception occurs when a product doesn't exist
                    System.out.println("Detected orphaned cart items, cleaning up...");
                    // Delete all cart items for this cart to reset
                    cartItemRepository.deleteByCart_CartId(cart.getCartId());
                    allItems = new java.util.ArrayList<>();
                }
                
                // Also try to remove items individually in case of partial failures
                cart.getCartItems().clear();
                for (CartItem item : allItems) {
                    try {
                        // Try to access the product - if it exists, keep the item
                        if (item.getProduct() != null && item.getProduct().getProductId() != null) {
                            cart.addCartItem(item);
                        } else {
                            System.out.println("Removing null product item: " + item.getCartItemId());
                            cartItemRepository.delete(item);
                        }
                    } catch (Exception e) {
                        System.out.println("Removing orphaned cart item: " + item.getCartItemId() + " - " + e.getMessage());
                        try {
                            cartItemRepository.delete(item);
                        } catch (Exception deleteEx) {
                            System.out.println("Failed to delete orphaned item: " + deleteEx.getMessage());
                        }
                    }
                }
                
                if (!cart.getCartItems().isEmpty()) {
                    shoppingCartRepository.save(cart);
                }
            }
            
            model.addAttribute("cart", cart);
            model.addAttribute("user", user);
            model.addAttribute("customer", customer);
            
            // Calculate average ratings for all cart items
            java.util.Map<String, Double> productRatings = new java.util.HashMap<>();
            for (CartItem item : cart.getCartItems()) {
                if (item.getProduct() != null) {
                    List<Review> reviews = reviewRepository.findByProduct_ProductId(item.getProduct().getProductId());
                    if (reviews != null && !reviews.isEmpty()) {
                        double avgRating = reviews.stream()
                            .mapToDouble(Review::getRating)
                            .average()
                            .orElse(0.0);
                        productRatings.put(item.getProduct().getProductId(), avgRating);
                    } else {
                        productRatings.put(item.getProduct().getProductId(), 0.0);
                    }
                }
            }
            model.addAttribute("productRatings", productRatings);
            
            return "customer/cart";
        } catch (Exception e) {
            System.out.println("Error viewing cart: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/customer/dashboard";
        }
    }

    @PostMapping("/cart/update")
    public String updateCartItem(@RequestParam Long cartItemId,
                                @RequestParam Integer quantity,
                                HttpSession session,
                                RedirectAttributes redirect) {
        
        if (!isCustomerLoggedIn(session)) {
            return "redirect:/login";
        }

        try {
            Optional<CartItem> itemOpt = cartItemRepository.findById(cartItemId);
            
            if (itemOpt.isEmpty()) {
                redirect.addFlashAttribute("error", "Item not found");
                return "redirect:/customer/cart";
            }

            CartItem item = itemOpt.get();
            
            if (quantity <= 0) {
                cartItemRepository.delete(item);
                redirect.addFlashAttribute("success", "Item removed from cart");
            } else {
                item.setQuantity(quantity);
                item.setUpdatedAt(LocalDateTime.now());
                cartItemRepository.save(item);
                redirect.addFlashAttribute("success", "Item updated");
            }
            
            return "redirect:/customer/cart";
        } catch (Exception e) {
            System.out.println("Error updating cart item: " + e.getMessage());
            redirect.addFlashAttribute("error", "Failed to update cart item");
            return "redirect:/customer/cart";
        }
    }

    @PostMapping("/cart/remove/{cartItemId}")
    public String removeFromCart(@PathVariable Long cartItemId,
                               HttpSession session,
                               RedirectAttributes redirect) {
        
        if (!isCustomerLoggedIn(session)) {
            return "redirect:/login";
        }

        try {
            Optional<CartItem> itemOpt = cartItemRepository.findById(cartItemId);
            
            if (itemOpt.isPresent()) {
                cartItemRepository.delete(itemOpt.get());
                redirect.addFlashAttribute("success", "Item removed from cart");
            } else {
                redirect.addFlashAttribute("error", "Item not found");
            }
            
            return "redirect:/customer/cart";
        } catch (Exception e) {
            System.out.println("Error removing cart item: " + e.getMessage());
            redirect.addFlashAttribute("error", "Failed to remove item");
            return "redirect:/customer/cart";
        }
    }

    @PostMapping("/cart/checkout")
    public String checkoutFromCart(HttpSession session, RedirectAttributes redirect) {
        
        if (!isCustomerLoggedIn(session)) {
            return "redirect:/login";
        }

        try {
            User user = getLoggedInUser(session);
            Optional<Customer> customerOpt = customerRepository.findByUser(user);

            if (customerOpt.isEmpty()) {
                redirect.addFlashAttribute("error", "Customer not found");
                return "redirect:/customer/dashboard";
            }

            Customer customer = customerOpt.get();
            Optional<ShoppingCart> cartOpt = shoppingCartRepository.findByCustomer(customer);

            if (cartOpt.isEmpty() || cartOpt.get().getCartItems().isEmpty()) {
                redirect.addFlashAttribute("error", "Your cart is empty");
                return "redirect:/customer/cart";
            }

            // Store cart ID in session for checkout process
            session.setAttribute("cartCheckout", cartOpt.get().getCartId());
            
            return "redirect:/customer/checkout";
        } catch (Exception e) {
            System.out.println("Error during cart checkout: " + e.getMessage());
            redirect.addFlashAttribute("error", "Failed to proceed to checkout");
            return "redirect:/customer/cart";
        }
    }

 @GetMapping("/checkout")
public String checkout(HttpSession session, Model model) {
    
    if (!isCustomerLoggedIn(session)) {
        return "redirect:/login";
    }

    try {
        User user = getLoggedInUser(session);
        Optional<Customer> customerOpt = customerRepository.findByUser(user);

        if (customerOpt.isEmpty()) {
            return "redirect:/customer/dashboard";
        }

        Customer customer = customerOpt.get();
        Optional<ShoppingCart> cartOpt = shoppingCartRepository.findByCustomer(customer);

        if (cartOpt.isEmpty() || cartOpt.get().getCartItems().isEmpty()) {
            return "redirect:/customer/cart?error=Cart is empty";
        }

        ShoppingCart cart = cartOpt.get();
        
        
        // Get available vouchers
        List<PromotionVoucher> availableVouchers = new ArrayList<>();
        if (customer.getIsMember() != null && customer.getIsMember()) {
            LocalDateTime now = LocalDateTime.now();
            availableVouchers = promotionVoucherRepository.findAll().stream()
                    .filter(v -> v.getIsActive() != null && v.getIsActive())
                    .filter(v -> v.getEndDate() == null || v.getEndDate().isAfter(now))
                    .filter(v -> v.getStartDate() == null || v.getStartDate().isBefore(now))
                    .filter(v -> v.getMaxUsage() == null || v.getCurrentUsage() < v.getMaxUsage())
                    .toList();
            
            System.out.println("📦 Total vouchers found: " + availableVouchers.size());
        }
        
        model.addAttribute("cart", cart);
        model.addAttribute("cartId", cart.getCartId());
        model.addAttribute("customer", customer);
        model.addAttribute("user", user);
        model.addAttribute("availableVouchers", availableVouchers);
        model.addAttribute("isDirectPurchase", false);
        
        return "customer/checkout";
    } catch (Exception e) {
        System.out.println("Error in cart checkout: " + e.getMessage());
        e.printStackTrace();
        return "redirect:/customer/dashboard";
    }
}

 @PostMapping("/checkout-confirm")
@Transactional
public String checkoutConfirm(@RequestParam(required = false) String recipientName,
                             @RequestParam(required = false) String phoneNumber,
                             @RequestParam(required = false) String shippingAddress,
                             @RequestParam(required = false) String city,
                             @RequestParam(required = false) String country,
                             @RequestParam(required = false) String voucherId,
                             @RequestParam(required = false) Long cartId,
                             @RequestParam(required = false) Boolean directPurchase,
                             @RequestParam(required = false) BigDecimal discountedTotal,
                             HttpSession session,
                             RedirectAttributes redirect) {
    
    System.out.println("\n" + "=".repeat(70));
    System.out.println("🛒 === CHECKOUT CONFIRM STARTED ===");
    System.out.println("=".repeat(70));
    System.out.println("Direct Purchase: " + directPurchase);
    System.out.println("Discounted Total: " + discountedTotal);
    System.out.println("Voucher ID: " + voucherId);
    
    if (!isCustomerLoggedIn(session)) {
        System.out.println("❌ Customer not logged in");
        return "redirect:/login";
    }

    try {
        User user = getLoggedInUser(session);
        System.out.println("✅ User: " + user.getUsername());
        
        Optional<Customer> customerOpt = customerRepository.findByUser(user);
        if (customerOpt.isEmpty()) {
            System.out.println("❌ Customer not found");
            redirect.addFlashAttribute("error", "Customer not found");
            return "redirect:/customer/dashboard";
        }
        
        Customer customer = customerOpt.get();
        System.out.println("✅ Customer: " + customer.getFullname());
        
        // ===== FIND CART =====
        Optional<ShoppingCart> cartOpt = Optional.empty();
        
        if (directPurchase != null && directPurchase) {
            ShoppingCart tempCart = (ShoppingCart) session.getAttribute("directPurchaseCart");
            if (tempCart != null && !tempCart.getCartItems().isEmpty()) {
                cartOpt = Optional.of(tempCart);
                System.out.println("✅ Using direct purchase cart with " + tempCart.getCartItems().size() + " items");
            } else {
                System.out.println("❌ Direct purchase cart not found in session");
                redirect.addFlashAttribute("error", "Product not found");
                return "redirect:/customer/dashboard";
            }
        } else {
            if (cartId != null && cartId > 0) {
                System.out.println("🔍 Looking for cart by ID: " + cartId);
                cartOpt = shoppingCartRepository.findById(cartId);
            }
            if (cartOpt.isEmpty()) {
                System.out.println("🔍 Looking for cart by customer...");
                cartOpt = shoppingCartRepository.findByCustomer(customer);
            }
        }
        
        if (cartOpt.isEmpty()) {
            System.out.println("❌ Cart not found!");
            redirect.addFlashAttribute("error", "Shopping cart not found");
            return "redirect:/customer/cart";
        }
        
        ShoppingCart cart = cartOpt.get();
        System.out.println("✅ Cart found: Items=" + cart.getCartItems().size());
        
        if (cart.getCartItems().isEmpty()) {
            System.out.println("❌ Cart is empty!");
            redirect.addFlashAttribute("error", "Your cart is empty");
            return "redirect:/customer/cart";
        }
        
        // ===== CREATE ORDER =====
        System.out.println("📝 Creating order...");
        Order order = new Order(customer);
        order.setShippingAddress(shippingAddress + ", " + city + ", " + country);
        order.setOrderStatus("PENDING");
        order.setPaymentStatus("PENDING");

        // Calculate original total from cart items
        BigDecimal originalTotal = BigDecimal.ZERO;
        for (CartItem item : cart.getCartItems()) {
            BigDecimal itemTotal = BigDecimal.valueOf(item.getProduct().getPrice() * item.getQuantity());
            originalTotal = originalTotal.add(itemTotal);
            System.out.println("   Item: " + item.getProduct().getProductName() + " = RM" + itemTotal);
        }
        System.out.println("📊 Original Total: RM" + originalTotal);
        
        // ✅ CALCULATE DISCOUNT
        BigDecimal finalTotal = originalTotal;
        BigDecimal discountAmount = BigDecimal.ZERO;
        
        if (discountedTotal != null && discountedTotal.compareTo(BigDecimal.ZERO) > 0) {
            if (discountedTotal.compareTo(originalTotal) < 0) {
                finalTotal = discountedTotal;
                discountAmount = originalTotal.subtract(discountedTotal);
                System.out.println("✅ Using discounted total: RM" + finalTotal);
                System.out.println("   Discount amount: RM" + discountAmount);
            }
        }
        
        // ✅ SAVE ORDER WITH ORIGINAL TOTAL FIRST (will be overwritten later)
        order.setTotalPrice(originalTotal);
        order = orderRepository.save(order);
        System.out.println("✅ Order saved! ID: " + order.getOrderId());

        // ===== CREATE ORDER ITEMS =====
        System.out.println("📦 Creating order items...");
        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();
            if (product != null) {
                OrderItem orderItem = new OrderItem(
                    order,
                    product,
                    cartItem.getQuantity(),
                    BigDecimal.valueOf(product.getPrice())
                );
                
                orderItemRepository.save(orderItem);
                order.addOrderItem(orderItem);
                
                System.out.println("   ✅ Added: " + product.getProductName() + 
                                " x" + cartItem.getQuantity() + 
                                " = RM" + orderItem.getSubtotal());
            }
        }
        
        // ✅ FORCE SET THE DISCOUNTED TOTAL AFTER ITEMS ARE ADDED
        // This prevents recalculateTotal() from overwriting the discount
        order.setTotalPrice(finalTotal);
        order = orderRepository.save(order);
        System.out.println("✅ Order total FORCE SET to: RM" + order.getTotalPrice());
        System.out.println("✅ All order items saved! Total items: " + order.getOrderItems().size());
        
        // ===== APPLY VOUCHER =====
        if (voucherId != null && !voucherId.isEmpty()) {
            try {
                Integer voucherIdInt = Integer.parseInt(voucherId);
                Optional<PromotionVoucher> voucherOpt = promotionVoucherRepository.findById(voucherIdInt);
                if (voucherOpt.isPresent()) {
                    PromotionVoucher voucher = voucherOpt.get();
                    System.out.println("✅ Applying voucher: " + voucher.getVoucherCode());
                    
                    OrderVoucher orderVoucher = new OrderVoucher();
                    orderVoucher.setOrderId(order.getOrderId().intValue());
                    orderVoucher.setVoucherId(voucherIdInt);
                    orderVoucher.setCustomerId(customer.getCustomerId());
                    orderVoucher.setDiscountAmount(discountAmount);
                    orderVoucher.setUsedDate(LocalDateTime.now());
                    orderVoucherRepository.save(orderVoucher);
                    
                    System.out.println("✅ Voucher applied: " + voucher.getVoucherCode() + 
                                     " (Discount: RM" + discountAmount + ")");
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error applying voucher: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // ===== CLEAR CART =====
        if (directPurchase == null || !directPurchase) {
            cart.getCartItems().clear();
            shoppingCartRepository.save(cart);
            System.out.println("✅ Cart cleared");
        } else {
            session.removeAttribute("directPurchaseCart");
            session.removeAttribute("isDirectPurchase");
            System.out.println("✅ Direct purchase cart removed from session");
        }
        
        // ===== CREATE PAYMENT WITH DISCOUNTED TOTAL =====
        System.out.println("💳 Creating ToyyibPay payment...");
        System.out.println("   Order ID: " + order.getOrderId());
        System.out.println("   Original Total: RM" + originalTotal);
        System.out.println("   Discount: RM" + discountAmount);
        System.out.println("   Final Total (to be charged): RM" + order.getTotalPrice());
        
        // ✅ VERIFY: Double check the order total before sending to ToyyibPay
        System.out.println("🔍 VERIFYING ORDER TOTAL BEFORE PAYMENT: RM" + order.getTotalPrice());
        
        String returnUrl = "https://pitaperfume-production.up.railway.app/payment/return?orderId=" + order.getOrderId();
        Payment payment = toyyibPayService.createPaymentBill(order, returnUrl);
        
        if (payment != null && payment.getPaymentUrl() != null) {
            System.out.println("✅ Payment created! Redirecting to: " + payment.getPaymentUrl());
            System.out.println("   Amount to be paid: RM" + order.getTotalPrice());
            return "redirect:" + payment.getPaymentUrl();
        } else {
            System.err.println("❌ Payment creation failed!");
            redirect.addFlashAttribute("error", "Failed to create payment");
            return "redirect:/customer/cart";
        }
        
    } catch (Exception e) {
        System.err.println("❌ ERROR: " + e.getMessage());
        e.printStackTrace();
        redirect.addFlashAttribute("error", "Checkout failed: " + e.getMessage());
        return "redirect:/customer/cart";
    }
}
@GetMapping("/order-history")
public String orderHistory(HttpSession session, Model model) {
    if (!isCustomerLoggedIn(session)) {
        return "redirect:/login";
    }
    
    System.out.println("\n" + "=".repeat(70));
    System.out.println("📋 === ORDER HISTORY REQUEST ===");
    System.out.println("=".repeat(70));
    
    User user = getLoggedInUser(session);
    Optional<Customer> customerOpt = customerRepository.findByUser(user);
    
    if (customerOpt.isEmpty()) {
        System.out.println("❌ Customer not found");
        return "redirect:/login";
    }
    
    Customer customer = customerOpt.get();
    System.out.println("✅ Customer: " + customer.getFullname());
    System.out.println("✅ Customer ID: " + customer.getCustomerId());
    
    // ✅ ORDER BY orderDate DESC - Latest first
    List<Order> orders = orderRepository.findByCustomer_CustomerIdOrderByOrderDateDesc((long) customer.getCustomerId());
    System.out.println("📦 Total orders found: " + orders.size());
    
    List<Order> validOrders = new java.util.ArrayList<>();
    
    for (Order order : orders) {
        // Skip if not PAID
        if (!"PAID".equals(order.getPaymentStatus())) {
            System.out.println("   ⏭️ Skipping order #" + order.getOrderId() + " (not paid)");
            continue;
        }
        
        System.out.println("   ✅ Processing order #" + order.getOrderId());
        
        // ✅ Get order items
        List<OrderItem> items = orderItemRepository.findByOrder_OrderId(order.getOrderId());
        System.out.println("      📦 Found " + items.size() + " items");
        
        List<OrderItem> validItems = new java.util.ArrayList<>();
        
        for (OrderItem item : items) {
            try {
                String productId = null;
                try {
                    if (item.getProduct() != null) {
                        productId = item.getProduct().getProductId();
                    }
                } catch (Exception e) {
                    System.out.println("      ⚠️ Lazy loading failed for item: " + item.getOrderItemId());
                }
                
                if (productId == null) {
                    System.out.println("      ⚠️ Product ID is null - skipping item");
                    continue;
                }
                
                System.out.println("      🔍 Checking product: " + productId);
                
                Optional<Product> productOpt = productRepository.findById(productId);
                if (productOpt.isPresent()) {
                    Product product = productOpt.get();
                    item.setProduct(product);
                    validItems.add(item);
                    System.out.println("      ✅ Product found: " + product.getProductName());
                } else {
                    System.out.println("      ⚠️ Product '" + productId + "' not found in database - skipping");
                }
                
            } catch (Exception e) {
                System.out.println("      ❌ Error processing item: " + e.getMessage());
                System.out.println("      ❌ Item ID: " + item.getOrderItemId());
                e.printStackTrace();
            }
        }
        
        if (!validItems.isEmpty()) {
            order.setOrderItems(validItems);
            validOrders.add(order);
            System.out.println("   ✅ Order #" + order.getOrderId() + " has " + validItems.size() + " valid items");
            System.out.println("   ✅ Order total (discounted): RM" + order.getTotalPrice());
        } else {
            System.out.println("   ⏭️ Order #" + order.getOrderId() + " has no valid items - skipping");
        }
    }
    
    System.out.println("📦 Final valid orders: " + validOrders.size());
    
    // ✅ Get products this customer has already reviewed
    List<Review> customerReviews = new java.util.ArrayList<>();
    try {
        customerReviews = reviewRepository.findByCustomer_CustomerId((long) customer.getCustomerId());
    } catch (Exception e) {
        System.out.println("❌ Error loading reviews: " + e.getMessage());
    }
    
    java.util.Set<String> reviewedProductIds = customerReviews.stream()
        .filter(review -> review.getProduct() != null)
        .map(review -> review.getProduct().getProductId())
        .collect(java.util.stream.Collectors.toSet());
    
    System.out.println("📝 Products already reviewed: " + reviewedProductIds.size());
    
    model.addAttribute("orders", validOrders);
    model.addAttribute("customer", customer);
    model.addAttribute("user", user);
    model.addAttribute("reviewedProductIds", reviewedProductIds);
    
    return "customer/order-history";
}

    // ========== MEMBERSHIP APPLICATION ==========
    @GetMapping("/apply-membership")
    public String applyMembershipForm(HttpSession session, Model model) {
        if (!isCustomerLoggedIn(session)) {
            return "redirect:/login";
        }
        
        User user = getLoggedInUser(session);
        Optional<Customer> customerOpt = customerRepository.findByUser(user);
        
        if (customerOpt.isEmpty()) {
            return "redirect:/customer/dashboard";
        }
        
        Customer customer = customerOpt.get();
        
        // Check if already a member
        if (customer.getIsMember() != null && customer.getIsMember()) {
            return "redirect:/customer/dashboard?info=You are already a member";
        }
        
        // Check if already has a pending application
        Optional<MembershipApplication> existingApp = membershipApplicationRepository.findByCustomer(customer);
        if (existingApp.isPresent()) {
            model.addAttribute("existingApplication", existingApp.get());
        }
        
        model.addAttribute("customer", customer);
        return "customer/apply-membership";
    }
    
    @PostMapping("/apply-membership")
    public String submitMembershipApplication(
            @RequestParam(required = false) Boolean termsAccepted,
            HttpSession session,
            RedirectAttributes redirect) {
        
        if (!isCustomerLoggedIn(session)) {
            return "redirect:/login";
        }
        
        if (termsAccepted == null || !termsAccepted) {
            redirect.addFlashAttribute("error", "You must accept the terms and conditions to apply for membership");
            return "redirect:/customer/apply-membership";
        }
        
        User user = getLoggedInUser(session);
        Optional<Customer> customerOpt = customerRepository.findByUser(user);
        
        if (customerOpt.isEmpty()) {
            redirect.addFlashAttribute("error", "Customer not found");
            return "redirect:/customer/dashboard";
        }
        
        Customer customer = customerOpt.get();
        
        // Check if already a member
        if (customer.getIsMember() != null && customer.getIsMember()) {
            redirect.addFlashAttribute("info", "You are already a member");
            return "redirect:/customer/dashboard";
        }
        
        // Check if already has a pending/approved application
        Optional<MembershipApplication> existingApp = membershipApplicationRepository.findByCustomer(customer);
        if (existingApp.isPresent()) {
            MembershipApplication app = existingApp.get();
            if ("PENDING".equals(app.getStatus())) {
                redirect.addFlashAttribute("error", "You already have a pending membership application");
                return "redirect:/customer/apply-membership";
            } else if ("APPROVED".equals(app.getStatus())) {
                redirect.addFlashAttribute("info", "Your membership has been approved!");
                return "redirect:/customer/dashboard";
            }
        }
        
        // Create new membership application
        MembershipApplication application = new MembershipApplication();
        application.setCustomer(customer);
        application.setStatus("PENDING");
        application.setTermsAccepted(true);
        application.setAppliedDate(LocalDateTime.now());
        
        membershipApplicationRepository.save(application);
        
        redirect.addFlashAttribute("success", "Your membership application has been submitted! Our manager will review it shortly.");
        return "redirect:/customer/dashboard";
    }
}


