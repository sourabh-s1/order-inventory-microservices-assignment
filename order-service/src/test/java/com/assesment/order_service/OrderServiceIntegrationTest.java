package com.assesment.order_service;

import com.assesment.order_service.dto.PlaceOrderRequest;
import com.assesment.order_service.entity.OrderEntity;
import com.assesment.order_service.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY) // use in-memory DB for repositories
@Transactional
class OrderServiceIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @MockitoBean
    private RestTemplate externalRestTemplate;

    private final String INVENTORY_BASE_URL = "http://inventory-service";

    @BeforeEach
    void beforeEach() {
        orderRepository.deleteAll();
    }

    @Test
    void placeOrder_whenEnoughStock_shouldReturnSuccess_andPersistOrder() {
        PlaceOrderRequest request = new PlaceOrderRequest(1, 6);

        Map<String, Object> batch1 = new HashMap<>();
        batch1.put("batchNumber", "b1");
        batch1.put("quantity", 4);

        Map<String, Object> batch2 = new HashMap<>();
        batch2.put("batchNumber", "b2");
        batch2.put("quantity", 5);

        List<Map<String, Object>> batches = Arrays.asList(batch1, batch2);

        when(externalRestTemplate.getForEntity(INVENTORY_BASE_URL + "/inventory/" + request.getProductId(), List.class))
                .thenReturn(new ResponseEntity<>(batches, HttpStatus.OK));

        when(externalRestTemplate.postForEntity(eq(INVENTORY_BASE_URL + "/inventory/update"), any(), eq(Void.class)))
                .thenReturn(new ResponseEntity<Void>(HttpStatus.OK));

        ResponseEntity<OrderEntity> response = testRestTemplate.postForEntity(
                "http://localhost:" + port + "/order",
                request,
                OrderEntity.class
        );


        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OrderEntity respBody = response.getBody();
        assertThat(respBody).isNotNull();
        assertThat(respBody.getProductId()).isEqualTo(request.getProductId());
        assertThat(respBody.getQuantity()).isEqualTo(request.getQuantity());
        assertThat(respBody.getStatus()).isEqualTo("SUCCESS");

        List<OrderEntity> saved = orderRepository.findAll();
        assertThat(saved).hasSize(1);
        OrderEntity savedOrder = saved.get(0);
        assertThat(savedOrder.getProductId()).isEqualTo(1);
        assertThat(savedOrder.getStatus()).isEqualTo("SUCCESS");

        verify(externalRestTemplate, times(1))
                .postForEntity(eq(INVENTORY_BASE_URL + "/inventory/update"), any(), eq(Void.class));
    }

    @Test
    void placeOrder_whenNotEnoughStock_shouldReturnNotEnoughStatus_andPersistOrder() {
        PlaceOrderRequest request = new PlaceOrderRequest(2, 10);

        Map<String, Object> batch = new HashMap<>();
        batch.put("batchNumber", "b1");
        batch.put("quantity", 3);

        List<Map<String, Object>> batches = Collections.singletonList(batch);

        when(externalRestTemplate.getForEntity(INVENTORY_BASE_URL + "/inventory/" + request.getProductId(), List.class))
                .thenReturn(new ResponseEntity<>(batches, HttpStatus.OK));

        ResponseEntity<OrderEntity> response = testRestTemplate.postForEntity(
                "http://localhost:" + port + "/order",
                request,
                OrderEntity.class
        );


        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OrderEntity respBody = response.getBody();
        assertThat(respBody).isNotNull();
        assertThat(respBody.getProductId()).isEqualTo(request.getProductId());
        assertThat(respBody.getQuantity()).isEqualTo(request.getQuantity());
        assertThat(respBody.getStatus()).containsIgnoringCase("Not enough stock");

        List<OrderEntity> saved = orderRepository.findAll();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getStatus()).containsIgnoringCase("Not enough stock");

        verify(externalRestTemplate, never())
                .postForEntity(eq(INVENTORY_BASE_URL + "/inventory/update"), any(), eq(Void.class));
    }
}
