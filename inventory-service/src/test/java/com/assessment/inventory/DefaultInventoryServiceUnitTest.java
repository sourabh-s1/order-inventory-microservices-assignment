package com.assessment.inventory;

import com.assessment.inventory.dto.InventoryBatchDTO;
import com.assessment.inventory.dto.InventoryUpdateRequest;
import com.assessment.inventory.entity.InventoryBatch;
import com.assessment.inventory.handler.InventoryHandler;
import com.assessment.inventory.repository.InventoryBatchRepository;
import com.assessment.inventory.service.implementation.DefaultInventoryService;
import com.assessment.inventory.service.implementation.InventoryHandlerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultInventoryServiceUnitTest {

    @Mock
    private InventoryBatchRepository inventoryBatchRepository;

    @Mock
    private InventoryHandlerFactory inventoryHandlerFactory;

    @Mock
    private InventoryHandler inventoryHandler;

    @InjectMocks
    private DefaultInventoryService inventoryService;

    @Test
    void getBatchesByProductSorted_returnsDtoList_sortedByExpiry() {
        InventoryBatch b1 = InventoryBatch.builder()
                .id(1L).batchNumber("B1").quantity(5).expiryDate(LocalDate.now().plusDays(10)).build();

        InventoryBatch b2 = InventoryBatch.builder()
                .id(2L).batchNumber("B2").quantity(3).expiryDate(LocalDate.now().plusDays(5)).build();

        when(inventoryBatchRepository.findByProductIdOrderByExpiryDateAsc(100L))
                .thenReturn(Arrays.asList(b2, b1)); // already ordered

        List<InventoryBatchDTO> dtos = inventoryService.getBatchesByProductSorted(100L);

        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getBatchNumber()).isEqualTo("B2");
        assertThat(dtos.get(1).getBatchNumber()).isEqualTo("B1");
    }

    @Test
    void updateInventory_delegatesToHandler_and_returnsDtos() {
        InventoryUpdateRequest req = new InventoryUpdateRequest();
        req.setProductId(100L);
        Map<String,Integer> ded = new HashMap<>();
        ded.put("B1", 2);
        req.setBatchQuantityToDeduct(ded);

        InventoryBatch updated = InventoryBatch.builder()
                .id(1L).batchNumber("B1").quantity(3).expiryDate(LocalDate.now().plusDays(5)).build();

        when(inventoryHandlerFactory.getHandler(anyString())).thenReturn(inventoryHandler);
        when(inventoryHandler.handleUpdate(req)).thenReturn(Collections.singletonList(updated));

        List<InventoryBatchDTO> result = inventoryService.updateInventory(req);

        assertThat(result).hasSize(1);
        InventoryBatchDTO dto = result.get(0);
        assertThat(dto.getBatchNumber()).isEqualTo("B1");
        assertThat(dto.getQuantity()).isEqualTo(3);

        verify(inventoryHandlerFactory).getHandler("defaultInventoryHandler");
        verify(inventoryHandler).handleUpdate(req);
    }
}