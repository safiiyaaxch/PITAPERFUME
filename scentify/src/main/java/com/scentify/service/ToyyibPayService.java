package com.scentify.service;

import com.scentify.model.Order;
import com.scentify.model.OrderItem;
import com.scentify.model.Payment;
import com.scentify.repository.PaymentRepository;
import com.scentify.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
public class ToyyibPayService {
    
    @Value("${toyyibpay.api-secret-key}")
    private String apiSecretKey;
    
    @Value("${toyyibpay.category-code}")
    private String categoryCode;

    @Value("${toyyibpay.sandbox-mode:false}")
    private boolean sandboxMode;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.payment.return-url:${app.base-url}/payment/return}")
    private String configuredReturnUrl;

    @Value("${app.payment.callback-url:${app.base-url}/payment/callback}")
    private String configuredCallbackUrl;
    
    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    private static final String PROD_CREATE_BILL_URL = "https://toyyibpay.com/index.php/api/createBill";
    private static final String SANDBOX_CREATE_BILL_URL = "https://dev.toyyibpay.com/index.php/api/createBill";
    private static final String PROD_CHECK_BILL_URL = "https://toyyibpay.com/index.php/api/getBillTransactions";
    private static final String SANDBOX_CHECK_BILL_URL = "https://dev.toyyibpay.com/index.php/api/getBillTransactions";
    
    // HARDCODED PRODUCTION URL FOR TESTING
    private static final String PRODUCTION_RETURN_URL = "https://pitaperfume-production.up.railway.app/payment/return";
    private static final String PRODUCTION_CALLBACK_URL = "https://pitaperfume-production.up.railway.app/payment/callback";
    
    private String getCreateBillUrl() {
        return sandboxMode ? SANDBOX_CREATE_BILL_URL : PROD_CREATE_BILL_URL;
    }
    
    private String getCheckBillUrl() {
        return sandboxMode ? SANDBOX_CHECK_BILL_URL : PROD_CHECK_BILL_URL;
    }
    
    /**
     * Create a ToyyibPay bill for the order with multiple items support
     * Reference: https://toyyibpay.com/apireference/
     */
    public Payment createPaymentBill(Order order, String returnUrl) {
        try {
            String createBillUrl = getCreateBillUrl();
            
            System.out.println("🚨🚨🚨 ===== TOYYIBPAY CREATE BILL ===== 🚨🚨🚨");
            System.out.println("📌 Environment: " + (sandboxMode ? "SANDBOX (Testing)" : "PRODUCTION"));
            System.out.println("📌 Base URL from config: " + baseUrl);
            System.out.println("📌 Configured Return URL: " + configuredReturnUrl);
            System.out.println("📌 Configured Callback URL: " + configuredCallbackUrl);
            System.out.println("📌 Passed Return URL parameter: " + returnUrl);
            
            // ✅ FORCE HARDCODED URLS FOR RAILWAY DEPLOYMENT
            String finalReturnUrl = PRODUCTION_RETURN_URL;
            String finalCallbackUrl = PRODUCTION_CALLBACK_URL;
            
            System.out.println("🔥🔥🔥 USING HARDCODED RETURN URL: " + finalReturnUrl);
            System.out.println("🔥🔥🔥 USING HARDCODED CALLBACK URL: " + finalCallbackUrl);
            
            // Create payment record
            Payment payment = new Payment(order, order.getTotalPrice());
            
            // Convert amount to cents
            long amountInCents = order.getTotalPrice().multiply(new BigDecimal("100")).longValue();
            System.out.println("💰 Amount: RM " + order.getTotalPrice() + " = " + amountInCents + " cents");
            
            // Build bill description with all items
            String billDescription = buildBillDescription(order);
            System.out.println("📝 Bill Description: " + billDescription);
            
            // Build bill name
            String billName = "Scentify Order #" + order.getOrderId();
            if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
                int itemCount = order.getOrderItems().size();
                billName += " (" + itemCount + " item" + (itemCount > 1 ? "s" : "") + ")";
            }
            
            // Get validated phone number
            String phone = getValidPhoneNumber(order);
            System.out.println("📞 Phone: " + phone);
            
            // Prepare ToyyibPay request
            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("userSecretKey", apiSecretKey);
            requestBody.add("categoryCode", categoryCode);
            requestBody.add("billName", billName);
            requestBody.add("billDescription", billDescription);
            requestBody.add("billPriceSetting", "1");
            requestBody.add("billPayorInfo", "1");
            requestBody.add("billAmount", String.valueOf(amountInCents));
            requestBody.add("billReturnUrl", finalReturnUrl);  // ✅ HARDCODED
            requestBody.add("billCallbackUrl", finalCallbackUrl);  // ✅ HARDCODED
            requestBody.add("billExternalReferenceNo", "ORDER-" + order.getOrderId());
            requestBody.add("billTo", order.getCustomer().getFullname());
            requestBody.add("billEmail", order.getCustomer().getUser().getEmail());
            requestBody.add("billPhone", phone);
            requestBody.add("billPaymentChannel", "2");
            
            // Build email content
            String emailContent = buildEmailContent(order);
            requestBody.add("billContentEmail", emailContent);
            
            System.out.println("📤 Sending request to ToyyibPay...");
            System.out.println("📤 URL: " + createBillUrl);
            System.out.println("📤 Bill Name: " + billName);
            System.out.println("📤 Bill Description: " + billDescription);
            System.out.println("📤 Return URL being sent: " + finalReturnUrl);
            System.out.println("📤 Callback URL being sent: " + finalCallbackUrl);
            System.out.println("📤 Phone: " + phone);
            
            // Send request to ToyyibPay
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);
            
            // Get response as String first to handle HTML error responses
            String responseBody = restTemplate.postForObject(createBillUrl, request, String.class);
            
            System.out.println("📦 Raw response received (length: " + (responseBody != null ? responseBody.length() : "NULL") + ")");
            if (responseBody != null && responseBody.length() > 0) {
                System.out.println("📦 Response preview: " + responseBody.substring(0, Math.min(500, responseBody.length())));
            }
            
            if (responseBody == null || responseBody.trim().isEmpty()) {
                System.err.println("❌ Response body is null or empty!");
                return null;
            }
            
            // Try to parse as JSON
            Map<String, Object> response = null;
            ObjectMapper objectMapper = new ObjectMapper();
            
            try {
                response = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
                System.out.println("✅ Successfully parsed response as JSON Object");
            } catch (Exception e1) {
                try {
                    java.util.List<Map<String, Object>> responseArray = objectMapper.readValue(responseBody, new TypeReference<java.util.List<Map<String, Object>>>() {});
                    System.out.println("⚠️ Response is a JSON array - extracting first element");
                    
                    if (responseArray != null && !responseArray.isEmpty()) {
                        response = responseArray.get(0);
                        System.out.println("✅ Extracted first element from array: " + response);
                    } else {
                        System.err.println("❌ Response array is empty!");
                        return null;
                    }
                } catch (Exception e2) {
                    System.err.println("⚠️ Failed to parse response as JSON: " + e1.getMessage());
                    System.err.println("📝 Raw response body: " + responseBody);
                    return null;
                }
            }
                
            if (response == null) {
                System.err.println("❌ Response is null");
                return null;
            }
            
            System.out.println("📊 Full response: " + response);
            
            // Check for error status
            if (response.containsKey("status") && "error".equals(response.get("status"))) {
                String errorMsg = response.containsKey("msg") ? (String) response.get("msg") : "Unknown error";
                System.err.println("❌ ToyyibPay error: " + errorMsg);
                return null;
            }
            
            // Try different possible response formats
            String billCode = null;
            if (response.containsKey("BillCode")) {
                billCode = (String) response.get("BillCode");
            } else if (response.containsKey("billcode")) {
                billCode = (String) response.get("billcode");
            } else if (response.containsKey("data")) {
                Object dataObj = response.get("data");
                if (dataObj instanceof Map) {
                    Map<String, Object> dataMap = (Map<String, Object>) dataObj;
                    if (dataMap.containsKey("BillCode")) {
                        billCode = (String) dataMap.get("BillCode");
                    } else if (dataMap.containsKey("billcode")) {
                        billCode = (String) dataMap.get("billcode");
                    }
                }
            }
            
            if (billCode != null && !billCode.isEmpty()) {
                payment.setBillCode(billCode);
                String paymentUrl = (sandboxMode ? "https://dev.toyyibpay.com/bill/?billCode=" : "https://toyyibpay.com/bill/?billCode=") + billCode;
                payment.setPaymentUrl(paymentUrl);
                payment = paymentRepository.save(payment);
                
                order.setToyyibPayBillCode(billCode);
                order.setPaymentStatus("PENDING");
                orderRepository.save(order);
                
                System.out.println("✅ ToyyibPay bill created successfully!");
                System.out.println("✅ Bill Code: " + billCode);
                System.out.println("✅ Payment URL: " + paymentUrl);
                System.out.println("✅ Return URL stored in bill: " + finalReturnUrl);
                System.out.println("✅ Order saved with bill code: " + order.getOrderId());
                return payment;
            } else {
                System.err.println("❌ Failed to extract BillCode from ToyyibPay response");
                System.err.println("❌ Response: " + response);
                if (response.containsKey("status")) {
                    System.err.println("❌ Response status: " + response.get("status"));
                }
                if (response.containsKey("msg")) {
                    System.err.println("❌ Response message: " + response.get("msg"));
                }
                return null;
            }
        } catch (Exception e) {
            System.err.println("❌ Error creating ToyyibPay bill: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get and validate phone number from order
     */
    private String getValidPhoneNumber(Order order) {
        String phone = "";
        
        try {
            // Try to get phone from customer
            if (order.getCustomer() != null) {
                if (order.getCustomer().getPhone() != null && !order.getCustomer().getPhone().isEmpty()) {
                    phone = order.getCustomer().getPhone();
                    System.out.println("Phone from customer: " + phone);
                }
            }
            
            // If phone is empty, try to get from shipping address if available
            if (phone.isEmpty() && order.getShippingAddress() != null) {
                // You might have phone in shipping address
                // phone = order.getShippingPhone();
            }
            
            // Clean the phone number
            if (!phone.isEmpty()) {
                // Remove all non-digit characters
                phone = phone.replaceAll("[^0-9]", "");
                
                // Validate Malaysian phone number
                if (phone.startsWith("0") && phone.length() >= 10 && phone.length() <= 11) {
                    System.out.println("Valid Malaysian phone number: " + phone);
                } else if (phone.length() == 10 && !phone.startsWith("0")) {
                    // If no leading 0, add it
                    phone = "0" + phone;
                    System.out.println("Added leading 0: " + phone);
                } else if (phone.length() == 9 && phone.startsWith("1")) {
                    // For numbers like 123456789 -> 0123456789
                    phone = "0" + phone;
                    System.out.println("Added leading 0: " + phone);
                } else {
                    System.out.println("Invalid phone format: " + phone + ", using default");
                    phone = "0123456789";
                }
            } else {
                System.out.println("No phone found, using default");
                phone = "0123456789";
            }
        } catch (Exception e) {
            System.err.println("Error getting phone: " + e.getMessage());
            phone = "0123456789";
        }
        
        return phone;
    }
    
    /**
     * Build bill description with all items in the order
     */
    private String buildBillDescription(Order order) {
        StringBuilder description = new StringBuilder();
        description.append("Order for ").append(order.getCustomer().getFullname());
        
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            description.append(" - ");
            for (int i = 0; i < order.getOrderItems().size(); i++) {
                OrderItem item = order.getOrderItems().get(i);
                if (i > 0) {
                    if (i == order.getOrderItems().size() - 1) {
                        description.append(" & ");
                    } else {
                        description.append(", ");
                    }
                }
                description.append(item.getProduct().getProductName())
                          .append(" x").append(item.getQuantity());
            }
        }
        
        // Truncate if too long (ToyyibPay has limits)
        String result = description.toString();
        if (result.length() > 100) {
            result = result.substring(0, 97) + "...";
        }
        
        return result;
    }
    
    /**
     * Build email content with all items
     */
    private String buildEmailContent(Order order) {
        StringBuilder content = new StringBuilder();
        content.append("Thank you for your order at Scentify!\n\n");
        content.append("Order #").append(order.getOrderId()).append("\n");
        content.append("Date: ").append(LocalDateTime.now()).append("\n\n");
        content.append("Items:\n");
        content.append("-".repeat(30)).append("\n");
        
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            for (OrderItem item : order.getOrderItems()) {
                content.append(item.getQuantity())
                       .append("x ")
                       .append(item.getProduct().getProductName())
                       .append(" - RM ")
                       .append(item.getSubtotal())
                       .append("\n");
            }
        }
        
        content.append("-".repeat(30)).append("\n");
        content.append("Total: RM ").append(order.getTotalPrice()).append("\n\n");
        content.append("Shipping Address: ").append(order.getShippingAddress()).append("\n\n");
        content.append("Thank you for shopping with us!");
        
        return content.toString();
    }
    
    /**
     * Check payment status via API
     */
    public Payment checkPaymentStatus(String billCode) {
        try {
            String checkBillUrl = getCheckBillUrl();
            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("billCode", billCode);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);
            
            Map<String, Object> response = restTemplate.postForObject(checkBillUrl, request, Map.class);
            
            if (response != null) {
                System.out.println("Payment status checked: " + response);
            }
        } catch (Exception e) {
            System.err.println("Error checking payment status: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Validate callback hash using MD5
     * Formula: MD5(userSecretKey + status + order_id + refno + "ok")
     */
    public boolean validateCallbackHash(String receivedHash, String status, String orderId, String refno) {
        try {
            String hashInput = apiSecretKey + status + orderId + refno + "ok";
            String expectedHash = generateMD5(hashInput);
            
            boolean isValid = expectedHash.equalsIgnoreCase(receivedHash);
            System.out.println("Hash Validation - Expected: " + expectedHash + ", Received: " + receivedHash + ", Valid: " + isValid);
            return isValid;
        } catch (Exception e) {
            System.err.println("Error validating callback hash: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Generate MD5 hash
     */
    private String generateMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : messageDigest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            System.err.println("Error generating MD5: " + e.getMessage());
            return "";
        }
    }
}