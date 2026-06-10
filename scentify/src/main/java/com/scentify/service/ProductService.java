package com.scentify.service;

import com.scentify.model.Product;
import com.scentify.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for product management operations
 */
@Service
@Slf4j
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    /**
     * Check if a product is owned by a specific supplier
     * 
     * @param productId the product ID
     * @param supplierId the supplier ID
     * @return true if the product is owned by the supplier, false otherwise
     */
    public boolean isProductOwnedBySupplier(String productId, String supplierId) {
        try {
            Product product = productRepository.findById(productId).orElse(null);
            
            if (product == null) {
                log.warn("Product {} not found", productId);
                return false;
            }
            
            Integer supplierIdInt = Integer.parseInt(supplierId);
            boolean owns = product.getUserId() != null && product.getUserId().equals(supplierIdInt);
            
            if (!owns) {
                log.warn("Product {} is not owned by supplier {}", productId, supplierId);
            }
            
            return owns;
        } catch (NumberFormatException e) {
            log.error("Invalid supplier ID format: {}", supplierId);
            return false;
        } catch (Exception e) {
            log.error("Error checking product ownership", e);
            return false;
        }
    }

    /**
     * Get a product by ID
     */
    public Product getProductById(String productId) {
        return productRepository.findById(productId).orElse(null);
    }

    /**
     * Save a product
     */
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
}
