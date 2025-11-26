package com.assessment.inventory.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryBatchDTO {
    private Long id;
    private String batchNumber;
    private Integer quantity;
    private LocalDate expiryDate;
}
