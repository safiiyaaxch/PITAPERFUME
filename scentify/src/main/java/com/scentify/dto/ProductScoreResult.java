package com.scentify.dto;

import com.scentify.model.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for quiz recommendation results
 * Includes product details, score, and confidence based on data completeness
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductScoreResult {
    private Product product;
    private double finalScore;
    private int rawScore;
    private int confidencePercent;  // 10-95%
    private int dataCompletenessLayers;  // 0-3 (how many of: topNotes, middleNotes, baseNotes are populated)
}
