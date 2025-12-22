package com.fiap.snackapp.adapters.driver.api.controller;

import com.fiap.snackapp.core.application.dto.request.AddOnCreateRequest;
import com.fiap.snackapp.core.application.dto.request.AddOnUpdateRequest;
import com.fiap.snackapp.core.application.dto.request.ProductCreateRequest;
import com.fiap.snackapp.core.application.dto.request.ProductUpdateRequest;
import com.fiap.snackapp.core.application.dto.response.AddOnResponse;
import com.fiap.snackapp.core.application.dto.response.ProductResponse;
import com.fiap.snackapp.core.application.exception.ResourceNotFoundException;
import com.fiap.snackapp.core.application.usecases.ProductUseCase;
import com.fiap.snackapp.core.domain.enums.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductUseCase productUseCase;

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse product = productUseCase.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getProductsByFilters(
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "category", required = false) Category category) {

        List<ProductResponse> products = productUseCase.getProductsByFilters(active, category);

        return ResponseEntity.ok(products);
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductCreateRequest createRequest) {
        ProductResponse createdProduct = productUseCase.createProduct(createRequest);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductUpdateRequest productUpdateRequest) {
        ProductResponse product = productUseCase.updateProduct(id, productUpdateRequest);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ProductResponse> setProductStatusToFalse(@PathVariable Long id) {
        ProductUpdateRequest productUpdateRequest = new ProductUpdateRequest(null, null, null, null, false);
        ProductResponse product = productUseCase.updateProduct(id, productUpdateRequest);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/add-ons/{id}")
    public ResponseEntity<AddOnResponse> getAddOnById(@PathVariable Long id) {
        AddOnResponse addOn = productUseCase.getAddOnById(id);
        return ResponseEntity.ok(addOn);
    }

    @GetMapping("/add-ons")
    public ResponseEntity<List<AddOnResponse>> getAddOnsByFilters(
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "category", required = false) Category category) {

        List<AddOnResponse> addOns = productUseCase.getAddOnsByFilters(active, category);

        return ResponseEntity.ok(addOns);
    }

    @PostMapping("/add-ons")
    public ResponseEntity<AddOnResponse> createAddOn(@RequestBody AddOnCreateRequest createRequest) {
        AddOnResponse createAddOn = productUseCase.createAddOn(createRequest);
        return new ResponseEntity<>(createAddOn, HttpStatus.CREATED);
    }

    @PatchMapping("/add-ons/{id}")
    public ResponseEntity<AddOnResponse> updateAddOnStatus(
            @PathVariable Long id,
            @RequestBody AddOnUpdateRequest addOnUpdateRequest) {
        AddOnResponse addOn = productUseCase.updateAddOn(id, addOnUpdateRequest);
        return ResponseEntity.ok(addOn);
    }

    @DeleteMapping("/add-ons/{id}")
    public ResponseEntity<AddOnResponse> setAddOnStatusToFalse(@PathVariable Long id) {
        AddOnResponse addOn = productUseCase.updateAddOn(id, new AddOnUpdateRequest(null, null, null, false));
        return ResponseEntity.ok(addOn);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFound(ResourceNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
}