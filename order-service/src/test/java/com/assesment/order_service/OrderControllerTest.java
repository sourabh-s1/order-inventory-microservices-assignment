package com.assesment.order_service;


import com.assesment.order_service.controller.OrderController;
import com.assesment.order_service.dto.PlaceOrderRequest;
import com.assesment.order_service.entity.OrderEntity;
import com.assesment.order_service.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    OrderService orderService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void placeOrder_returnsOrderEntity() throws Exception {
        PlaceOrderRequest req = new PlaceOrderRequest(1, 2);
        OrderEntity respEntity = new OrderEntity(1L, 1, 2, "SUCCESS");

        when(orderService.placeOrder(any(PlaceOrderRequest.class))).thenReturn(respEntity);

        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
}