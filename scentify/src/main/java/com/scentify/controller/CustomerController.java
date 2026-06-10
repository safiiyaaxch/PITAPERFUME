package com.scentify.controller;

import com.scentify.model.Customer;
import com.scentify.model.Product;
import com.scentify.model.PromotionVoucher;
import com.scentify.model.User;
import com.scentify.model.ShoppingCart;
import com.scentify.model.CartItem;
import com.scentify.model.Order;
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
import com.scentify.service.ToyyibPayService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
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
            
            // Filter by minimum rating
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
        // Show all reviews (no approval needed)
        
        // Calculate review statistics
        if (reviews != null && !reviews.isEmpty()) {
            // Calculate average rating
            double averageRating = reviews.stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);
            
            // Calculate star distribution
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
        
        // Check if current customer can write review
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
                // Check if customer purchased this product
                List<Order> orders = orderRepository.findByCustomer_CustomerId((long) customer.getCustomerId());
                boolean hasPurchased = orders.stream()
                    .anyMatch(order -> id.equals(order.getProduct().getProductId()) && 
                                     ("CONFIRMED".equals(order.getOrderStatus()) || "DELIVERED".equals(order.getOrderStatus()) || "SHIPPED".equals(order.getOrderStatus())));
                
                if (!hasPurchased) {
                    reviewBlockReason = "You can only review products you've purchased";
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
        if (customer.getIsMember() != null && customer.getIsMember()) {
            availableVouchers = promotionVoucherRepository.findAll().stream()
                    .filter(v -> v.getEndDate() != null && v.getEndDate().isAfter(LocalDateTime.now()))
                    .filter(v -> v.getIsActive())
                    .filter(v -> v.getVoucherProducts() != null && v.getVoucherProducts().stream()
                            .anyMatch(vp -> vp.getProductId().equals(productId)))
                    .toList();
        }
        
        model.addAttribute("product", product);
        model.addAttribute("customer", customer);
        model.addAttribute("user", user);
        model.addAttribute("availableVouchers", availableVouchers);
        
        // Create a dummy cart with the product for single product checkout
        ShoppingCart dummyCart = new ShoppingCart();
        CartItem dummyItem = new CartItem();
        dummyItem.setProduct(product);
        dummyItem.setQuantity(1);
        dummyCart.addCartItem(dummyItem);
        model.addAttribute("cart", dummyCart);
        
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
            
            // Get available vouchers for cart items
            List<PromotionVoucher> availableVouchers = List.of();
            if (customer.getIsMember() != null && customer.getIsMember()) {
                availableVouchers = promotionVoucherRepository.findAll().stream()
                        .filter(v -> v.getEndDate() != null && v.getEndDate().isAfter(LocalDateTime.now()))
                        .filter(v -> v.getIsActive())
                        .toList();
            }
            
            model.addAttribute("cart", cart);
            model.addAttribute("cartId", cart.getCartId());
            model.addAttribute("customer", customer);
            model.addAttribute("user", user);
            model.addAttribute("availableVouchers", availableVouchers);
            
            return "customer/checkout";
        } catch (Exception e) {
            System.out.println("Error in cart checkout: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/customer/dashboard";
        }
    }

    @PostMapping("/checkout-confirm")
    public String checkoutConfirm(@RequestParam(required = false) String recipientName,
                                 @RequestParam(required = false) String phoneNumber,
                                 @RequestParam(required = false) String shippingAddress,
                                 @RequestParam(required = false) String city,
                                 @RequestParam(required = false) String country,
                                 @RequestParam(required = false) String voucherId,
                                 @RequestParam(required = false) Long cartId,
                                 HttpSession session,
                                 RedirectAttributes redirect) {
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📍 === CHECKOUT CONFIRM STARTED ===");
        System.out.println("=".repeat(60));
        System.out.println("📝 Params - Name: " + recipientName + ", Phone: " + phoneNumber);
        System.out.println("📝 Params - Address: " + shippingAddress + ", City: " + city + ", Country: " + country);
        System.out.println("📝 Params - VoucherId: " + voucherId + ", CartId: " + cartId);
        
        if (!isCustomerLoggedIn(session)) {
            System.out.println("❌ Customer not logged in");
            return "redirect:/login";
        }

        try {
            User user = getLoggedInUser(session);
            System.out.println("✅ User logged in: " + user.getUsername() + " (ID: " + user.getUserId() + ")");
            
            Optional<Customer> customerOpt = customerRepository.findByUser(user);

            if (customerOpt.isEmpty()) {
                System.out.println("❌ Customer not found for userId: " + user.getUserId());
                redirect.addFlashAttribute("error", "Customer not found");
                return "redirect:/customer/dashboard";
            }

            Customer customer = customerOpt.get();
            System.out.println("✅ Customer found: " + customer.getFullname() + " (ID: " + customer.getCustomerId() + ")");
            
            // Try to find cart by cartId if provided, otherwise find by customer
            Optional<ShoppingCart> cartOpt = Optional.empty();
            if (cartId != null && cartId > 0) {
                System.out.println("🔍 Looking up cart by cartId: " + cartId);
                cartOpt = shoppingCartRepository.findById(cartId);
            }
            if (cartOpt.isEmpty()) {
                System.out.println("🔍 Looking up cart by customer...");
                cartOpt = shoppingCartRepository.findByCustomer(customer);
            }

            if (cartOpt.isEmpty()) {
                System.out.println("❌ Cart not found for customer ID: " + customer.getCustomerId());
                redirect.addFlashAttribute("error", "Shopping cart not found");
                return "redirect:/customer/cart";
            }

            ShoppingCart cart = cartOpt.get();
            
            if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
                System.out.println("❌ Cart is empty. No items in cart ID: " + cart.getCartId());
                redirect.addFlashAttribute("error", "Your cart is empty");
                return "redirect:/customer/cart";
            }

            System.out.println("✅ Cart found: ID=" + cart.getCartId() + ", Items=" + cart.getCartItems().size() + ", Total=RM" + cart.getTotalPrice());

            // Update customer with delivery phone
            customer.setPhone(phoneNumber);
            customerRepository.save(customer);
            System.out.println("📞 Updated customer phone: " + phoneNumber);

            // Create Order with delivery details
            Order order = new Order();
            order.setCustomer(customer);
            order.setShippingAddress(shippingAddress + ", " + city + ", " + country);
            order.setOrderStatus("PENDING");
            order.setPaymentStatus("PENDING");
            order.setTotalPrice(new BigDecimal(String.valueOf(cart.getTotalPrice() + 4.90))); // Include delivery cost
            
            // Set first product from cart (using first item for initial order setup)
            if (!cart.getCartItems().isEmpty()) {
                order.setProduct(cart.getCartItems().get(0).getProduct());
                order.setQuantity(cart.getCartItems().stream()
                    .mapToInt(CartItem::getQuantity)
                    .sum());
            }
            
            order = orderRepository.save(order);
            System.out.println("✅ Order created: #" + order.getOrderId() + " with total RM " + order.getTotalPrice());
            
            // Process voucher if selected
            BigDecimal discountAmount = BigDecimal.ZERO;
            if (voucherId != null && !voucherId.isEmpty()) {
                try {
                    Integer vId = Integer.parseInt(voucherId);
                    Optional<PromotionVoucher> voucherOpt = promotionVoucherRepository.findById(vId);
                    
                    if (voucherOpt.isPresent()) {
                        PromotionVoucher voucher = voucherOpt.get();
                        System.out.println("🎫 Voucher found: " + voucher.getVoucherCode());
                        
                        // Calculate discount based on type
                        if (voucher.getDiscountType().equals(PromotionVoucher.DiscountType.PERCENTAGE)) {
                            // Apply percentage to product prices only (not including delivery)
                            discountAmount = new BigDecimal(String.valueOf(cart.getTotalPrice()))
                                    .multiply(voucher.getDiscountValue())
                                    .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                            System.out.println("💰 Percentage discount applied: " + voucher.getDiscountValue() + "% of RM " + cart.getTotalPrice() + " = RM " + discountAmount);
                        } else if (voucher.getDiscountType().equals(PromotionVoucher.DiscountType.FIXED_AMOUNT)) {
                            discountAmount = voucher.getDiscountValue();
                            System.out.println("💰 Fixed discount applied: RM " + discountAmount);
                        }
                        
                        // Update order total with discount
                        BigDecimal newTotal = order.getTotalPrice().subtract(discountAmount);
                        order.setTotalPrice(newTotal);
                        order = orderRepository.save(order);
                        System.out.println("✅ Order updated with discount. New total: RM " + order.getTotalPrice());
                        
                        // Create OrderVoucher record to track usage
                        OrderVoucher orderVoucher = new OrderVoucher();
                        orderVoucher.setOrderId(order.getOrderId().intValue());
                        orderVoucher.setVoucherId(vId);
                        orderVoucher.setCustomerId(customer.getCustomerId());
                        orderVoucher.setDiscountAmount(discountAmount);
                        orderVoucher.setUsedDate(LocalDateTime.now());
                        
                        // Save OrderVoucher record
                        orderVoucherRepository.save(orderVoucher);
                        System.out.println("📝 OrderVoucher record created successfully");
                        
                        // Increment voucher usage
                        voucher.incrementUsage();
                        promotionVoucherRepository.save(voucher);
                        System.out.println("📊 Voucher usage incremented");
                    } else {
                        System.out.println("⚠️ Voucher not found: " + vId);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("⚠️ Invalid voucherId format: " + voucherId);
                }
            }
            
            // Create ToyyibPay payment bill
            String returnUrl = "http://localhost:8080/payment/return?orderId=" + order.getOrderId();
            System.out.println("📍 Creating payment bill with return URL: " + returnUrl);
            
            Payment payment = toyyibPayService.createPaymentBill(order, returnUrl);
            
            if (payment != null && payment.getPaymentUrl() != null && !payment.getPaymentUrl().isEmpty()) {
                System.out.println("✅ Payment created successfully");
                System.out.println("📍 Payment URL: " + payment.getPaymentUrl());
                redirect.addFlashAttribute("info", "Redirecting to payment gateway...");
                return "redirect:" + payment.getPaymentUrl();
            } else {
                System.out.println("Payment is null or URL is empty. Payment: " + payment);
                if (payment != null) {
                    System.out.println("   Payment URL: " + payment.getPaymentUrl());
                    System.out.println("   Bill Code: " + payment.getBillCode());
                }
                redirect.addFlashAttribute("error", "Failed to create payment. Please try again.");
                return "redirect:/customer/cart";
            }
            
        } catch (Exception e) {
            System.out.println("Error during checkout confirmation: " + e.getMessage());
            e.printStackTrace();
            redirect.addFlashAttribute("error", "Failed to process checkout: " + e.getMessage());
            return "redirect:/customer/cart";
        }
    }
    
    // ========== ORDER HISTORY ==========
    @GetMapping("/order-history")
    public String orderHistory(HttpSession session, Model model) {
        if (!isCustomerLoggedIn(session)) {
            return "redirect:/login";
        }
        
        User user = getLoggedInUser(session);
        Optional<Customer> customerOpt = customerRepository.findByUser(user);
        
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            // Get all PAID orders for this customer
            List<Order> allOrders = orderRepository.findByCustomer_CustomerId((long) customer.getCustomerId());
            // Filter to only show paid and confirmed orders
            List<Order> orders = allOrders.stream()
                .filter(order -> "PAID".equals(order.getPaymentStatus()) && 
                               ("CONFIRMED".equals(order.getOrderStatus()) || 
                                "SHIPPED".equals(order.getOrderStatus()) || 
                                "DELIVERED".equals(order.getOrderStatus())))
                .toList();
            model.addAttribute("orders", orders);
            model.addAttribute("customer", customer);
            model.addAttribute("user", user);
            return "customer/order-history";
        }
        
        return "redirect:/login";
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


