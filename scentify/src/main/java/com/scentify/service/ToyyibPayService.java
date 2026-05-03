package com.scentify.service;

import com.scentify.model.Order;
import com.scentify.model.Payment;
import com.scentify.repository.PaymentRepository;
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
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    private static final String CREATE_BILL_URL = "https://toyyibpay.com/index.php/api/createBill";
    private static final String CHECK_BILL_URL = "https://toyyibpay.com/index.php/api/getBillTransactions";
    
    /**
     * Create a ToyyibPay bill for the order
     * Reference: https://toyyibpay.com/apireference/
     */
    public Payment createPaymentBill(Order order, String returnUrl) {
        try {
            System.out.println("=== CREATING TOYYIBPAY BILL ===");
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
            System.out.println(" URL: " + CREATE_BILL_URL);
            
            // Send request to ToyyibPay
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);
            
            // Get response as String first to handle HTML error responses
            String responseBody = restTemplate.postForObject(CREATE_BILL_URL, request, String.class);
            
            System.out.println("📦 Raw response (first 500 chars): " + (responseBody != null ? responseBody.substring(0, Math.min(500, responseBody.length())) : "NULL"));
            
            if (responseBody == null || responseBody.isEmpty()) {
                System.err.println("❌ Response body is null or empty!");
                return null;
            }
            
            // Try to parse as JSON
            Map response = null;
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                response = objectMapper.readValue(responseBody, new TypeReference<Map>(){});
                System.out.println("✅ Parsed as JSON: " + response);
            } catch (Exception jsonError) {
                System.err.println("⚠️  Failed to parse as JSON: " + jsonError.getMessage());
                System.err.println("📝 Response was probably HTML error page");
                System.err.println("📝 Full response: " + responseBody);
                
                // If it's HTML, it's an error from ToyyibPay
                if (responseBody.contains("<html") || responseBody.contains("<HTML")) {
                    System.err.println("❌ ToyyibPay returned HTML (error page)");
                    return null;
                }
                return null;
            }
            
            System.out.println("📦 Response received: " + response);
            System.out.println("📦 Response type: " + (response != null ? response.getClass().getName() : "NULL"));
            
            if (response == null) {
                System.err.println("❌ Response is null!");
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
                payment.setPaymentUrl("https://toyyibpay.com/" + billCode);
                payment = paymentRepository.save(payment);
                
                order.setToyyibPayBillCode(billCode);
                order.setPaymentStatus("PENDING");
                
                System.out.println("✅ ToyyibPay bill created successfully!");
                System.out.println("✅ Bill Code: " + billCode);
                System.out.println("✅ Payment URL: https://toyyibpay.com/" + billCode);
                return payment;
            } else {
                System.err.println("❌ Failed to extract BillCode from ToyyibPay response");
                System.err.println("❌ Response keys: " + (response != null ? response.keySet() : "NULL"));
                System.err.println("❌ Response values: " + response);
                System.err.println("❌ Status: " + response.get("status"));
                System.err.println("❌ Message: " + response.get("Message"));
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
            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("billCode", billCode);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);
            
            Map response = restTemplate.postForObject(CHECK_BILL_URL, request, Map.class);
            
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
