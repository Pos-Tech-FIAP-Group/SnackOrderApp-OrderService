package com.fiap.snackapp.adapters.driver.api.controller;

import com.fiap.snackapp.core.application.dto.request.OrderInitRequest;
import com.fiap.snackapp.core.application.dto.request.OrderItemsRequest;
import com.fiap.snackapp.core.application.dto.request.OrderPaymentCreateRequest;
import com.fiap.snackapp.core.application.dto.request.OrderStatusUpdateRequest;
import com.fiap.snackapp.core.application.dto.response.OrderResponse;
import com.fiap.snackapp.core.application.usecases.OrderUseCase;
import com.fiap.snackapp.core.domain.enums.OrderStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderUseCase orderUseCase;

    @PostMapping("/init")
    public ResponseEntity<OrderResponse> init(
            @RequestBody @Valid OrderInitRequest request,
            HttpServletRequest httpRequest) {
        // Priorizar CPF do header (validado pelo Lambda)
        String cpfFromHeader = httpRequest.getHeader("X-CPF");
        String cpfToUse = (cpfFromHeader != null && !cpfFromHeader.isBlank())
                ? cpfFromHeader
                : request.cpf();  // Fallback para body

        OrderResponse response = orderUseCase.initOrder(cpfToUse);

        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/{orderId}/item")
    public ResponseEntity<OrderResponse> addItems(
            @PathVariable Long orderId,
            @RequestBody @Valid OrderItemsRequest request) {
        OrderResponse response = orderUseCase.addItems(orderId, request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping
    public ResponseEntity<Void> requestOrderPaymentCreation(OrderPaymentCreateRequest orderPaymentCreateRequest) {
        orderUseCase.requestOrderPaymentCreation(orderPaymentCreateRequest);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .build();
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody @Valid OrderStatusUpdateRequest request) {
        orderUseCase.updateOrderStatus(orderId, request);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> listAllOrdersByFilters(
            @RequestParam(name = "status", required = false) List<OrderStatus> orderStatus) {
        return ResponseEntity.ok(orderUseCase.listAllOrdersByFilters(orderStatus));
    }
}
