package com.mvc.mock_project.controller.api.owner;

import com.mvc.mock_project.dto.request.ProductFormDTO;
import com.mvc.mock_project.dto.response.ApiResponse;
import com.mvc.mock_project.dto.response.ProductCategoryDTO;
import com.mvc.mock_project.dto.response.ProductDTO;
import com.mvc.mock_project.entities.enums.ProductType;
import com.mvc.mock_project.security.CustomUserDetails;
import com.mvc.mock_project.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
public class OwnerProductApiController {

    private final ProductService productService;

    @GetMapping("/facilities/{facilityId}/products")
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> getProducts(
            @PathVariable Integer facilityId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) ProductType type,
            @RequestParam(required = false) Boolean status,
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Page<ProductDTO> products = productService.getProductsByFacility(facilityId, userDetails.getAccount().getId(), search, categoryId, type, status, pageable);
        return ResponseEntity.ok(ApiResponse.success("success", products));
    }

    @PostMapping("/facilities/{facilityId}/products")
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(
            @PathVariable Integer facilityId,
            @Valid @ModelAttribute ProductFormDTO formDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ProductDTO product = productService.createProduct(facilityId, userDetails.getAccount().getId(), formDTO);
        return ResponseEntity.ok(ApiResponse.success("Thêm sản phẩm thành công", product));
    }

    @PutMapping("/facilities/{facilityId}/products/{productId}")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
            @PathVariable Integer facilityId,
            @PathVariable Integer productId,
            @Valid @ModelAttribute ProductFormDTO formDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ProductDTO product = productService.updateProduct(productId, facilityId, userDetails.getAccount().getId(), formDTO);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật sản phẩm thành công", product));
    }

    @PatchMapping("/facilities/{facilityId}/products/{productId}/toggle")
    public ResponseEntity<ApiResponse<Void>> toggleProductStatus(
            @PathVariable Integer facilityId,
            @PathVariable Integer productId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        productService.toggleProductStatus(productId, facilityId, userDetails.getAccount().getId());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công"));
    }

    @DeleteMapping("/facilities/{facilityId}/products/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Integer facilityId,
            @PathVariable Integer productId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        productService.deleteProduct(productId, facilityId, userDetails.getAccount().getId());
        return ResponseEntity.ok(ApiResponse.success("Xóa sản phẩm thành công"));
    }

    @GetMapping("/product-categories")
    public ResponseEntity<ApiResponse<List<ProductCategoryDTO>>> getCategories() {
        List<ProductCategoryDTO> categories = productService.getActiveCategories();
        return ResponseEntity.ok(ApiResponse.success("success", categories));
    }
}
