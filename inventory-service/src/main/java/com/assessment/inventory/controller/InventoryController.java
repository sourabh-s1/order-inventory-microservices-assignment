package com.assessment.inventory.controller;

import com.assessment.inventory.dto.InventoryBatchDTO;
import com.assessment.inventory.dto.InventoryUpdateRequest;
import com.assessment.inventory.service.InventoryService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/inventory")
public class InventoryController {
    private final InventoryService inventoryService;

    @GetMapping("/{productID}")
    public ResponseEntity<List<InventoryBatchDTO>> getBatches(@PathVariable("productID") Long productID){
        List<InventoryBatchDTO> result = inventoryService.getBatchesByProductSorted(productID);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/update")
    public ResponseEntity<List<InventoryBatchDTO>> updateInventory(@RequestBody InventoryUpdateRequest request) {
        List<InventoryBatchDTO> updated = inventoryService.updateInventory(request);
        return ResponseEntity.ok(updated);
    }
}
