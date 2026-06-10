package com.scentify.service;

import com.scentify.model.Order;
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
    
    private String getCreateBillUrl() {
        return sandboxMode ? SANDBOX_CREATE_BILL_URL : PROD_CREATE_BILL_URL;
    }
    
    private String getCheckBillUrl() {
        return sandboxMode ? SANDBOX_CHECK_BILL_URL : PROD_CHECK_BILL_URL;
    }
    
    /**
     * Create a ToyyibPay bill for the order
     * Reference: https://toyyibpay.com/apireference/
     */
    public Payment createPaymentBill(Order order, String returnUrl) {
        try {
            String createBillUrl = getCreateBillUrl();
            System.out.println("=== CREATING TOYYIBPAY BILL ===");
            System.out.println("🌐 Environment: " + (sandboxMode ? "SANDBOX (Testing)" : "PRODUCTION"));
            System.out.println("API Secret Key: " + (apiSecretKey != null ? apiSecretKey.substring(0, 10) + "..." : "NULL"));
            System.out.println("Category Code: " + categoryCode);
            
            // Create payment record
            Payment payment = new Payment(order, order.getTotalPrice());
            
            // Convert amount to cents (API expects amount in cents: e.g., 100 = RM1.00)
            long amountInCents = order.getTotalPrice().multiply(new BigDecimal("100")).longValue();
            System.out.println("Amount: RM " + order.getTotalPrice() + " = " + amountInCents + " cents");
            
            // Prepare ToyyibPay request according to official API
            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("userSecretKey", apiSecretKey);
            requestBody.add("categoryCode", categoryCode);
            requestBody.add("billName", "Scentify Order #" + order.getOrderId());
            requestBody.add("billDescription", "Order for " + order.getCustomer().getFullname());
            requestBody.add("billPriceSetting", "1"); // 1 = fixed amount
            requestBody.add("billPayorInfo", "1"); // 1 = require payer info
            requestBody.add("billAmount", String.valueOf(amountInCents)); // Amount in cents
            requestBody.add("billReturnUrl", returnUrl);
            requestBody.add("billCallbackUrl", "http://localhost:8080/payment/callback"); // Callback URL for payment status updates
            requestBody.add("billExternalReferenceNo", "ORDER-" + order.getOrderId());
            requestBody.add("billTo", order.getCustomer().getFullname());
            requestBody.add("billEmail", order.getCustomer().getUser().getEmail());
            requestBody.add("billPhone", order.getCustomer().getPhone());
            requestBody.add("billPaymentChannel", "2"); // 0=FPX, 1=Credit Card, 2=Both
            requestBody.add("billContentEmail", "Thank you for your order at Scentify!");
            
            System.out.println("Sending request to ToyyibPay...");
            System.out.println(" URL: " + createBillUrl);
            
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
            Map response = null;
            ObjectMapper objectMapper = new ObjectMapper();
            
            try {
                // Try parsing as object first
                response = objectMapper.readValue(responseBody, Map.class);
                System.out.println("✅ Successfully parsed response as JSON Object");
            } catch (Exception e1) {
                // If it fails, try parsing as array
                try {
                    java.util.List<Map> responseArray = objectMapper.readValue(responseBody, java.util.List.class);
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
            System.out.println("📊 Response map keys: " + response.keySet());
            System.out.println("📊 Full response: " + response);
            
            if (response == null || response.isEmpty()) {
                System.err.println("❌ Response map is empty!");
                return null;
            }
            
            // Try different possible response formats
            String billCode = null;
            if (response.containsKey("BillCode")) {
                billCode = (String) response.get("BillCode");
                System.out.println("✅ Found BillCode in response");
            } else if (response.containsKey("billcode")) {
                billCode = (String) response.get("billcode");
                System.out.println("✅ Found billcode (lowercase) in response");
            } else if (response.containsKey("data")) {
                // Sometimes response wraps data in an object
                Object dataObj = response.get("data");
                if (dataObj instanceof Map) {
                    Map dataMap = (Map) dataObj;
                    billCode = (String) dataMap.get("BillCode");
                    System.out.println("✅ Found BillCode in data wrapper");
                }
            }
            
            if (billCode != null && !billCode.isEmpty()) {
                payment.setBillCode(billCode);
                // Use correct URL based on environment
                String paymentUrl = (sandboxMode ? "https://dev.toyyibpay.com/bill/?billCode=" : "https://toyyibpay.com/bill/?billCode=") + billCode;
                payment.setPaymentUrl(paymentUrl);
                payment = paymentRepository.save(payment);
                
                order.setToyyibPayBillCode(billCode);
                order.setPaymentStatus("PENDING");
                orderRepository.save(order);  // ✅ SAVE THE ORDER WITH BILL CODE
                
                System.out.println("✅ ToyyibPay bill created successfully!");
                System.out.println("✅ Bill Code: " + billCode);
                System.out.println("✅ Payment URL: " + paymentUrl);
                System.out.println("✅ Order saved with bill code: " + order.getOrderId());
                return payment;
            } else {
                System.err.println("❌ Failed to extract BillCode from ToyyibPay response");
                System.err.println("❌ Response: " + response);
                System.err.println("❌ Response keys: " + response.keySet());
                System.err.println("❌ Response status: " + response.get("status"));
                System.err.println("❌ Response message: " + response.get("Message"));
                System.err.println("❌ Response error: " + response.get("error"));
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error creating ToyyibPay bill: " + e.getMessage());
            System.err.println("Exception type: " + e.getClass().getName());
            e.printStackTrace();
        }
        return null;
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
            
            Map response = restTemplate.postForObject(checkBillUrl, request, Map.class);
            
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
