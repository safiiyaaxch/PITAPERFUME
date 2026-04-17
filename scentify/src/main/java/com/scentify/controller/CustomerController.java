package com.scentify.controller;

import com.scentify.model.Customer;
import com.scentify.model.Product;
import com.scentify.model.PromotionVoucher;
import com.scentify.model.User;
import com.scentify.model.ShoppingCart;
import com.scentify.model.CartItem;
import com.scentify.repository.CustomerRepository;
import com.scentify.repository.ProductRepository;
import com.scentify.repository.PromotionVoucherRepository;
import com.scentify.repository.UserRepository;
import com.scentify.repository.ShoppingCartRepository;
import com.scentify.repository.CartItemRepository;
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
            
            // Get or create shopping cart
            Optional<ShoppingCart> cartOpt = shoppingCartRepository.findByCustomer(customer);
            ShoppingCart cart = cartOpt.orElseGet(() -> new ShoppingCart(customer));

            // Get product
            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isEmpty()) {
                redirect.addFlashAttribute("error", "Product not found");
                return "redirect:/customer/dashboard";
            }

            Product product = productOpt.get();
            
            // Check if product already in cart
            Optional<CartItem> existingItem = cartItemRepository
                    .findByCart_CartIdAndProduct_ProductId(cart.getCartId(), productId);
            
            if (existingItem.isPresent()) {
                // Update quantity
                CartItem item = existingItem.get();
                item.setQuantity(item.getQuantity() + quantity);
                item.setUpdatedAt(LocalDateTime.now());
                cartItemRepository.save(item);
            } else {
                // Add new item
                CartItem newItem = new CartItem(cart, product, quantity);
                cart.addCartItem(newItem);
            }

            cart.setUpdatedAt(LocalDateTime.now());
            shoppingCartRepository.save(cart);
            
            redirect.addFlashAttribute("success", "Product added to cart!");
            return "redirect:/customer/cart";
        } catch (Exception e) {
            System.out.println("Error adding to cart: " + e.getMessage());
            e.printStackTrace();
            redirect.addFlashAttribute("error", "Failed to add product to cart");
            return "redirect:/customer/dashboard";
        }
    }

    @GetMapping("/cart")
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

            ShoppingCart cart = cartOpt.orElseGet(() -> new ShoppingCart(customer));
            
            model.addAttribute("cart", cart);
            model.addAttribute("user", user);
            model.addAttribute("customer", customer);
            
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

            ShoppingCart cart = cartOpt.get();

            // Process stock reduction for each cart item
            for (CartItem item : cart.getCartItems()) {
                Optional<Product> productOpt = productRepository.findById(item.getProduct().getProductId());
                if (productOpt.isPresent()) {
                    Product product = productOpt.get();
                    
                    // Check if sufficient stock available
                    if (product.getStock() < item.getQuantity()) {
                        redirect.addFlashAttribute("error", 
                            "Insufficient stock for " + product.getProductName() + 
                            ". Available: " + product.getStock() + ", Requested: " + item.getQuantity());
                        return "redirect:/customer/cart";
                    }

                    // Reduce stock
                    product.setStock(product.getStock() - item.getQuantity());
                    product.setUpdatedAt(LocalDateTime.now());
                    productRepository.save(product);
                    
                    System.out.println("Stock reduced for product " + product.getProductId() + 
                            " by " + item.getQuantity() + " units. New stock: " + product.getStock());
                }
            }

            // Clear the shopping cart after successful checkout
            cartItemRepository.deleteAll(cart.getCartItems());
            cart.setUpdatedAt(LocalDateTime.now());
            shoppingCartRepository.save(cart);

            redirect.addFlashAttribute("success", 
                "Order placed successfully! Thank you for your purchase.");
            
            System.out.println("Checkout completed for customer " + customer.getCustomerId() + 
                    ". Cart cleared.");
            
            return "redirect:/customer/dashboard";
        } catch (Exception e) {
            System.out.println("Error during checkout confirmation: " + e.getMessage());
            e.printStackTrace();
            redirect.addFlashAttribute("error", "Failed to process checkout");
            return "redirect:/customer/cart";
        }
    }
}

