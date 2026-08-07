package com.mvc.mock_project.controller.api.favorite;

import com.mvc.mock_project.dto.response.ApiResponse;
import com.mvc.mock_project.security.CustomUserDetails;
import com.mvc.mock_project.service.FavoriteFacilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer/favorites")
@RequiredArgsConstructor
public class FavoriteApiController {

    private final FavoriteFacilityService favoriteFacilityService;

    @PostMapping("/{facilityId}/toggle")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> toggleFavorite(
            @AuthenticationPrincipal Object principal,
            @PathVariable Integer facilityId) {
        
        Integer accountId = getAccountIdFromPrincipal(principal);
        if (accountId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }
        
        boolean isFavorited = favoriteFacilityService.toggleFavorite(accountId, facilityId);
        
        Map<String, Boolean> data = new HashMap<>();
        data.put("favorited", isFavorited);
        
        String message = isFavorited ? "favorite.added" : "favorite.removed";
        return ResponseEntity.ok(ApiResponse.success(message, data));
    }

    @GetMapping("/ids")
    public ResponseEntity<ApiResponse<List<Integer>>> getFavoriteFacilityIds(
            @AuthenticationPrincipal Object principal) {
        
        Integer accountId = getAccountIdFromPrincipal(principal);
        if (accountId == null) {
            return ResponseEntity.ok(ApiResponse.success("success", List.of()));
        }
        
        List<Integer> favoriteIds = favoriteFacilityService.getFavoriteFacilityIds(accountId);
        
        return ResponseEntity.ok(ApiResponse.success("success", favoriteIds));
    }
    
    private Integer getAccountIdFromPrincipal(Object principal) {
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getAccount().getId();
        } else if (principal instanceof com.mvc.mock_project.security.CustomOAuth2User) {
            return ((com.mvc.mock_project.security.CustomOAuth2User) principal).getAccount().getId();
        }
        return null;
    }
}
