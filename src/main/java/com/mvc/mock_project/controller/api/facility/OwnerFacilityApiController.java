package com.mvc.mock_project.controller.api.facility;

import com.mvc.mock_project.dto.request.facility.*;
import com.mvc.mock_project.dto.response.facility.OwnerFacilityDetailDTO;
import com.mvc.mock_project.dto.response.facility.OwnerFacilityListDTO;
import com.mvc.mock_project.security.CustomUserDetails;
import com.mvc.mock_project.service.OwnerFacilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
public class OwnerFacilityApiController {

    private final OwnerFacilityService ownerFacilityService;

    @PostMapping("/facilities")
    public ResponseEntity<?> createFacility(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateFacilityRequest request) {
        ownerFacilityService.createFacility(userDetails.getAccount().getId(), request);
        return ResponseEntity.ok().body("Facility created successfully");
    }

    @PutMapping("/facilities/{id}")
    public ResponseEntity<?> updateFacility(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer id,
            @Valid @RequestBody UpdateFacilityRequest request) {
        ownerFacilityService.updateFacility(userDetails.getAccount().getId(), id, request);
        return ResponseEntity.ok().body("Facility updated successfully");
    }

    @DeleteMapping("/facilities/{id}")
    public ResponseEntity<?> deleteFacility(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer id) {
        ownerFacilityService.deleteFacility(userDetails.getAccount().getId(), id);
        return ResponseEntity.ok().body("Facility deactivated successfully");
    }

    @PostMapping("/facilities/{id}/resubmit")
    public ResponseEntity<?> resubmitFacility(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer id) {
        ownerFacilityService.resubmitFacility(userDetails.getAccount().getId(), id);
        return ResponseEntity.ok().body("Facility resubmitted for review");
    }

    @GetMapping("/facilities")
    public ResponseEntity<List<OwnerFacilityListDTO>> getMyFacilities(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ownerFacilityService.getMyFacilities(userDetails.getAccount().getId()));
    }

    @GetMapping("/facilities/{id}")
    public ResponseEntity<OwnerFacilityDetailDTO> getMyFacilityDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer id) {
        return ResponseEntity.ok(ownerFacilityService.getMyFacilityDetail(userDetails.getAccount().getId(), id));
    }

    @PutMapping("/facilities/{id}/staff")
    public ResponseEntity<?> assignStaffToFacility(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer id,
            @RequestBody AssignFacilityStaffRequest request) {
        ownerFacilityService.assignStaffToFacility(userDetails.getAccount().getId(), id, request);
        return ResponseEntity.ok().body("Staff assigned successfully");
    }

    // --- Images ---
    @PostMapping("/facilities/{id}/images")
    public ResponseEntity<com.mvc.mock_project.dto.response.facility.FacilityImageDTO> addFacilityImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer id,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "url", required = false) String url) {
        com.mvc.mock_project.dto.response.facility.FacilityImageDTO dto = ownerFacilityService
                .addImageToFacility(userDetails.getAccount().getId(), id, file, url);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/facilities/{id}/images/{imageId}/thumbnail")
    public ResponseEntity<?> setFacilityThumbnail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer id,
            @PathVariable Integer imageId) {
        ownerFacilityService.setFacilityThumbnail(userDetails.getAccount().getId(), id, imageId);
        return ResponseEntity.ok().body("Đặt làm ảnh đại diện thành công");
    }

    @DeleteMapping("/facilities/{id}/images/{imageId}")
    public ResponseEntity<?> deleteFacilityImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer id,
            @PathVariable Integer imageId) {
        ownerFacilityService.deleteFacilityImage(userDetails.getAccount().getId(), id, imageId);
        return ResponseEntity.ok().body("Xóa ảnh thành công");
    }

    // --- Sports ---
    @PostMapping("/facilities/{id}/sports")
    public ResponseEntity<?> addSportToFacility(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer id,
            @Valid @RequestBody AddFacilitySportRequest request) {
        ownerFacilityService.addSportToFacility(userDetails.getAccount().getId(), id, request);
        return ResponseEntity.ok().body("Sport added successfully");
    }

    @PutMapping("/facility-sports/{fsId}")
    public ResponseEntity<?> updateFacilitySport(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer fsId,
            @Valid @RequestBody AddFacilitySportRequest request) {
        ownerFacilityService.updateFacilitySport(userDetails.getAccount().getId(), fsId, request);
        return ResponseEntity.ok().body("Sport configuration updated");
    }

    @DeleteMapping("/facility-sports/{fsId}")
    public ResponseEntity<?> removeSportFromFacility(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer fsId) {
        ownerFacilityService.removeSportFromFacility(userDetails.getAccount().getId(), fsId);
        return ResponseEntity.ok().body("Sport removed from facility");
    }

    // --- Courts ---
    @PostMapping("/courts")
    public ResponseEntity<?> createCourt(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateCourtRequest request) {
        ownerFacilityService.createCourt(userDetails.getAccount().getId(), request);
        return ResponseEntity.ok().body("Court created successfully");
    }

    @PutMapping("/courts/{courtId}")
    public ResponseEntity<?> updateCourt(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer courtId,
            @Valid @RequestBody UpdateCourtRequest request) {
        ownerFacilityService.updateCourt(userDetails.getAccount().getId(), courtId, request);
        return ResponseEntity.ok().body("Court updated successfully");
    }

    @DeleteMapping("/courts/{courtId}")
    public ResponseEntity<?> deleteCourt(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer courtId,
            @RequestParam(defaultValue = "false") boolean forceDeactivate) {
        try {
            ownerFacilityService.deleteCourt(userDetails.getAccount().getId(), courtId, forceDeactivate);
            return ResponseEntity.ok().body("Xóa/Vô hiệu hóa sân thành công");
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.CONFLICT)
                    .body(com.mvc.mock_project.dto.response.ApiResponse.error("COURT_HAS_BOOKINGS"));
        }
    }

    @PutMapping("/courts/{courtId}/toggle")
    public ResponseEntity<?> toggleCourt(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer courtId) {
        ownerFacilityService.toggleCourtStatus(userDetails.getAccount().getId(), courtId);
        return ResponseEntity.ok().body("Court status toggled successfully");
    }

    // --- Price Rules ---
    @PostMapping("/price-rules")
    public ResponseEntity<?> createPriceRule(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreatePriceRuleRequest request) {
        ownerFacilityService.createPriceRule(userDetails.getAccount().getId(), request);
        return ResponseEntity.ok().body("Price rule created successfully");
    }

    @PostMapping("/price-rules/batch")
    public ResponseEntity<?> batchSavePriceRules(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody com.mvc.mock_project.dto.request.facility.BatchSavePriceRulesRequest request) {
        ownerFacilityService.batchSavePriceRules(userDetails.getAccount().getId(), request);
        return ResponseEntity.ok().body(java.util.Map.of("message", "Pricing configuration saved successfully"));
    }

    @PutMapping("/price-rules/{ruleId}")
    public ResponseEntity<?> updatePriceRule(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer ruleId,
            @Valid @RequestBody UpdatePriceRuleRequest request) {
        ownerFacilityService.updatePriceRule(userDetails.getAccount().getId(), ruleId, request);
        return ResponseEntity.ok().body("Price rule updated successfully");
    }

    @DeleteMapping("/price-rules/{ruleId}")
    public ResponseEntity<?> deletePriceRule(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer ruleId) {
        ownerFacilityService.deletePriceRule(userDetails.getAccount().getId(), ruleId);
        return ResponseEntity.ok().body("Price rule deleted successfully");
    }

}
