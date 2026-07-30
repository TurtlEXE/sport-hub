package com.mvc.mock_project.controller.api.admin;

import com.mvc.mock_project.dto.request.ProductCategoryRequest;
import com.mvc.mock_project.dto.response.ApiResponse;
import com.mvc.mock_project.dto.response.ProductCategoryDTO;
import com.mvc.mock_project.service.ProductCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/product-categories")
@RequiredArgsConstructor
public class AdminProductCategoryApiController {

    private final ProductCategoryService productCategoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductCategoryDTO>>> getAllCategories(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Success", productCategoryService.getAllCategories(pageable)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductCategoryDTO>> createCategory(@Valid @RequestBody ProductCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Created category successfully", productCategoryService.createCategory(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductCategoryDTO>> updateCategory(
            @PathVariable Integer id,
            @Valid @RequestBody ProductCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Updated category successfully", productCategoryService.updateCategory(id, request)));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<ApiResponse<Void>> toggleActiveStatus(@PathVariable Integer id) {
        productCategoryService.toggleActiveStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Toggled active status successfully"));
    }
}
