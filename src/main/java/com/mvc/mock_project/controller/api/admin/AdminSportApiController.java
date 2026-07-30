package com.mvc.mock_project.controller.api.admin;

import com.mvc.mock_project.dto.request.SportAttributeRequest;
import com.mvc.mock_project.dto.request.SportRequest;
import com.mvc.mock_project.dto.response.ApiResponse;
import com.mvc.mock_project.dto.response.SportAttributeDTO;
import com.mvc.mock_project.dto.response.SportDTO;
import com.mvc.mock_project.service.SportAttributeService;
import com.mvc.mock_project.service.SportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/sports")
@RequiredArgsConstructor
public class AdminSportApiController {

    private final SportService sportService;
    private final SportAttributeService sportAttributeService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<SportDTO>>> getAllSports(
            @RequestParam(required = false) Boolean isActive,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Success", sportService.getAllSports(isActive, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SportDTO>> getSportById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Success", sportService.getSportById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SportDTO>> createSport(@Valid @RequestBody SportRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Created successfully", sportService.createSport(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SportDTO>> updateSport(
            @PathVariable Integer id,
            @Valid @RequestBody SportRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Updated successfully", sportService.updateSport(id, request)));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<ApiResponse<Void>> toggleActiveStatus(@PathVariable Integer id) {
        sportService.toggleActiveStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Toggled active status successfully"));
    }

    // Sport Attributes Endpoints
    @GetMapping("/{sportId}/attributes")
    public ResponseEntity<ApiResponse<List<SportAttributeDTO>>> getAttributesBySportId(@PathVariable Integer sportId) {
        return ResponseEntity.ok(ApiResponse.success("Success", sportAttributeService.getAttributesBySportId(sportId)));
    }

    @PostMapping("/{sportId}/attributes")
    public ResponseEntity<ApiResponse<SportAttributeDTO>> createAttribute(
            @PathVariable Integer sportId,
            @Valid @RequestBody SportAttributeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Created attribute successfully", sportAttributeService.createAttribute(sportId, request)));
    }

    @PutMapping("/{sportId}/attributes/{attributeId}")
    public ResponseEntity<ApiResponse<SportAttributeDTO>> updateAttribute(
            @PathVariable Integer sportId,
            @PathVariable Integer attributeId,
            @Valid @RequestBody SportAttributeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Updated attribute successfully", sportAttributeService.updateAttribute(sportId, attributeId, request)));
    }

    @DeleteMapping("/{sportId}/attributes/{attributeId}")
    public ResponseEntity<ApiResponse<Void>> deleteAttribute(
            @PathVariable Integer sportId,
            @PathVariable Integer attributeId) {
        sportAttributeService.deleteAttribute(sportId, attributeId);
        return ResponseEntity.ok(ApiResponse.success("Deleted attribute successfully"));
    }
}
