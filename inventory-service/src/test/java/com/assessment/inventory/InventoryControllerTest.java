package com.assessment.inventory;

import com.assessment.inventory.controller.InventoryController;
import com.assessment.inventory.dto.InventoryBatchDTO;
import com.assessment.inventory.dto.InventoryUpdateRequest;
import com.assessment.inventory.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventoryService inventoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getBatches_endpoint_returnsSortedDtos() throws Exception {
        InventoryBatchDTO d1 = InventoryBatchDTO.builder()
                .id(1L).batchNumber("b1").quantity(5).expiryDate(LocalDate.now().plusDays(2)).build();

        InventoryBatchDTO d2 = InventoryBatchDTO.builder()
                .id(2L).batchNumber("b2").quantity(2).expiryDate(LocalDate.now().plusDays(5)).build();

        when(inventoryService.getBatchesByProductSorted(10L)).thenReturn(Arrays.asList(d1, d2));

        mockMvc.perform(get("/inventory/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].batchNumber").value("b1"))
                .andExpect(jsonPath("$[1].batchNumber").value("b2"));
    }

    @Test
    void updateInventory_endpoint_returnsUpdatedDtos() throws Exception {
        InventoryUpdateRequest req = new InventoryUpdateRequest();
        req.setProductId(10L);
        Map<String,Integer> ded = new HashMap<>();
        ded.put("b1", 1);
        req.setBatchQuantityToDeduct(ded);

        InventoryBatchDTO updated = InventoryBatchDTO.builder()
                .id(1L).batchNumber("b1").quantity(4).expiryDate(LocalDate.now().plusDays(2)).build();

        when(inventoryService.updateInventory(any(InventoryUpdateRequest.class)))
                .thenReturn(Collections.singletonList(updated));

        mockMvc.perform(post("/inventory/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].batchNumber").value("b1"))
                .andExpect(jsonPath("$[0].quantity").value(4));
    }
}
