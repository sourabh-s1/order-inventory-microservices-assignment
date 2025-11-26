package com.assessment.inventory.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class InventoryUpdateRequest {
    private Long productId;
    /**
     * Map from batchNumber -> quantityToDeduct
     * Example: { "B-001" : 5, "B-002" : 2 }
     */
    private Map<String, Integer> batchQuantityToDeduct;
}

