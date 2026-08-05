package com.mvc.mock_project.service;

import java.util.List;

public interface FavoriteFacilityService {
    boolean toggleFavorite(Integer accountId, Integer facilityId);
    List<Integer> getFavoriteFacilityIds(Integer accountId);
}
