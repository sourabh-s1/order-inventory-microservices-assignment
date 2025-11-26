package com.assessment.inventory.service;

import com.assessment.inventory.dto.InventoryBatchDTO;
import com.assessment.inventory.dto.InventoryUpdateRequest;

import java.util.List;

public interface InventoryService {
    List<InventoryBatchDTO> getBatchesByProductSorted(Long productID);
    List<InventoryBatchDTO> updateInventory(InventoryUpdateRequest request);
}

