package com.mvc.mock_project.service;

import com.mvc.mock_project.dto.response.VenueCardDTO;
import com.mvc.mock_project.dto.response.VenueDetailDTO;
import com.mvc.mock_project.entities.ProductCategory;
import com.mvc.mock_project.entities.Sport;

import java.util.List;

public interface FacilityService {
    List<Sport> getAllActiveSports();

    List<ProductCategory> getAllActiveProductCategories();

    List<VenueCardDTO> getFilteredFacilities(
            String keyword,
            String sportCode,
            Double minPrice,
            Double maxPrice,
            String province,
            List<Integer> categoryIds,
            Boolean onlyFavorites,
            Integer accountId
    );

    List<VenueCardDTO> getAllActiveVenues();

    VenueCardDTO getVenueById(Integer id);

    VenueDetailDTO getVenueDetailById(Integer id);

    List<String> getDistinctProvinces();
}
