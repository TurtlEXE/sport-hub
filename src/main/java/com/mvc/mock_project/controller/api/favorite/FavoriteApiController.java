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
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer facilityId) {
        
        Integer accountId = userDetails.getAccount().getId();
        boolean isFavorited = favoriteFacilityService.toggleFavorite(accountId, facilityId);
        
        Map<String, Boolean> data = new HashMap<>();
        data.put("favorited", isFavorited);
        
        String message = isFavorited ? "favorite.added" : "favorite.removed";
        return ResponseEntity.ok(ApiResponse.success(message, data));
    }

    @GetMapping("/ids")
    public ResponseEntity<ApiResponse<List<Integer>>> getFavoriteFacilityIds(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Integer accountId = userDetails.getAccount().getId();
        List<Integer> favoriteIds = favoriteFacilityService.getFavoriteFacilityIds(accountId);
        
        return ResponseEntity.ok(ApiResponse.success("success", favoriteIds));
    }
}
