package com.assessment.inventory.service.implementation;

import com.assessment.inventory.dto.InventoryBatchDTO;
import com.assessment.inventory.dto.InventoryUpdateRequest;
import com.assessment.inventory.entity.InventoryBatch;
import com.assessment.inventory.handler.InventoryHandler;
import com.assessment.inventory.repository.InventoryBatchRepository;
import com.assessment.inventory.repository.ProductRepository;
import com.assessment.inventory.service.InventoryService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DefaultInventoryService implements InventoryService {
    private final InventoryBatchRepository inventoryBatchRepository;
    private final InventoryHandlerFactory inventoryHandlerFactory;


    @Override
    public List<InventoryBatchDTO> getBatchesByProductSorted(Long productID) {
        List<InventoryBatch> batches = inventoryBatchRepository.findByProductIdOrderByExpiryDateAsc(productID);
        return batches.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<InventoryBatchDTO> updateInventory(InventoryUpdateRequest request) {
        InventoryHandler handler = inventoryHandlerFactory.getHandler("defaultInventoryHandler");
        List<InventoryBatch> updated = handler.handleUpdate(request);
        return updated.stream().map(this::toDto).collect(Collectors.toList());
    }

    private InventoryBatchDTO toDto(InventoryBatch b) {
        return InventoryBatchDTO.builder()
                .id(b.getId())
                .batchNumber(b.getBatchNumber())
                .quantity(b.getQuantity())
                .expiryDate(b.getExpiryDate())
                .build();
    }
}
