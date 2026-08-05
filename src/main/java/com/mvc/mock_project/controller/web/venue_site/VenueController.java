package com.mvc.mock_project.controller.web.venue_site;

import com.mvc.mock_project.service.FacilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class VenueController {

    private final FacilityService facilityService;

    @GetMapping("/venues")
    public String showVenues(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String keyword,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String sportCode,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Double minPrice,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Double maxPrice,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String province,
            @org.springframework.web.bind.annotation.RequestParam(required = false) java.util.List<Integer> amenities,
            jakarta.servlet.http.HttpServletRequest request,
            Model model) {
        
        model.addAttribute("sports", facilityService.getAllActiveSports());
        model.addAttribute("categories", facilityService.getAllActiveProductCategories());
        
        model.addAttribute("provinces", facilityService.getDistinctProvinces());
        
        // Use filtering
        java.util.List<com.mvc.mock_project.dto.response.VenueCardDTO> venues;
        if (keyword != null || sportCode != null || minPrice != null || maxPrice != null || province != null || amenities != null) {
            venues = facilityService.getFilteredFacilities(keyword, sportCode, minPrice, maxPrice, province, amenities);
        } else {
            venues = facilityService.getAllActiveVenues();
        }
        model.addAttribute("venues", venues);
        
        // Retain filter state in UI
        model.addAttribute("keyword", keyword);
        model.addAttribute("sportCode", sportCode);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("province", province);
        model.addAttribute("selectedAmenities", amenities != null ? amenities : new java.util.ArrayList<>());

        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With")) || request.getHeader("Fetch") != null) {
            return "venue/venues :: venueListFragment";
        }

        return "venue/venues";
    }

    @GetMapping("/api/venues/{id}")
    @org.springframework.web.bind.annotation.ResponseBody
    public com.mvc.mock_project.dto.response.VenueCardDTO getVenueApi(@org.springframework.web.bind.annotation.PathVariable Integer id) {
        return facilityService.getVenueById(id);
    }

    @GetMapping("/venues/{id}/popup")
    public String getVenuePopup(@org.springframework.web.bind.annotation.PathVariable Integer id, Model model) {
        model.addAttribute("venue", facilityService.getVenueDetailById(id));
        return "venue/fragments/venue-popup :: popupContent";
    }
}
