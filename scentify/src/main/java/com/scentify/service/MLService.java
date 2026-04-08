package com.scentify.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class MLService {
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    // Python ML service URL (adjust based on your setup)
    private static final String ML_SERVICE_URL = "http://localhost:5000/predict";
    
    /**
     * Call Python ML service to get recommendations
     * This is for future integration when you have the Python service running
     */
    public Map<String, Object> callMLService(Map<String, String> quizAnswers) {
        try {
            // TODO: Implement when Python ML service is ready
            // return restTemplate.postForObject(ML_SERVICE_URL, quizAnswers, Map.class);
            return null;
        } catch (Exception e) {
            System.out.println("⚠️ ML Service not available yet: " + e.getMessage());
            return null;
        }
    }
}
