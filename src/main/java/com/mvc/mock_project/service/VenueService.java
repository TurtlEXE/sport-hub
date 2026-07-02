package com.mvc.mock_project.service;

import com.mvc.mock_project.dto.response.VenueCardDTO;
import com.mvc.mock_project.entities.Facility;
import com.mvc.mock_project.entities.FacilityImage;
import com.mvc.mock_project.entities.FacilityPriceRule;
import com.mvc.mock_project.entities.FacilitySport;
import com.mvc.mock_project.entities.Sport;
import com.mvc.mock_project.repository.FacilityImageRepository;
import com.mvc.mock_project.repository.FacilityPriceRuleRepository;
import com.mvc.mock_project.repository.FacilityRepository;
import com.mvc.mock_project.repository.FacilitySportRepository;
import com.mvc.mock_project.repository.SportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class VenueService {

    private final FacilityRepository facilityRepository;
    private final SportRepository sportRepository;
    private final FacilityImageRepository facilityImageRepository;
    private final FacilityPriceRuleRepository facilityPriceRuleRepository;

    public List<Sport> getAllActiveSports() {
        return sportRepository.findByIsActiveTrue();
    }

    public List<VenueCardDTO> getAllActiveVenues() {
        List<Facility> facilities = facilityRepository.findByIsActiveTrue();
        List<VenueCardDTO> venueCards = new ArrayList<>();
        Random random = new Random();

        for (Facility facility : facilities) {
            // Get Thumbnail
            FacilityImage thumbnail = facilityImageRepository.findFirstByFacilityIdAndIsThumbnailTrue(facility.getId());
            String imageUrl = (thumbnail != null) ? thumbnail.getImagePath() : "https://via.placeholder.com/400x250?text=No+Image";

            // Get Sports
            List<String> sports = new ArrayList<>();
            Double minPrice = Double.MAX_VALUE;
            
            if (facility.getFacilitySports() != null) {
                for (FacilitySport fs : facility.getFacilitySports()) {
                    if (Boolean.TRUE.equals(fs.getIsActive())) {
                        sports.add(fs.getSport().getSportCode());
                        // Find min price for this sport
                        List<FacilityPriceRule> rules = facilityPriceRuleRepository.findByFacilitySportIdAndIsActiveTrue(fs.getId());
                        for (FacilityPriceRule rule : rules) {
                            if (rule.getPricePerSlot() != null && rule.getPricePerSlot().doubleValue() < minPrice) {
                                minPrice = rule.getPricePerSlot().doubleValue();
                            }
                        }
                    }
                }
            }
            
            if (minPrice == Double.MAX_VALUE) {
                minPrice = 0.0;
            }

            // Mock rating and review count
            double rating = 4.0 + random.nextDouble(); // 4.0 to 5.0
            int reviewCount = 20 + random.nextInt(100);

            // Mock amenities
            List<String> mockAmenities = Arrays.asList("Locker", "Parking");

            // Format opening hours
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            String openingHours = "";
            if (facility.getOpenTime() != null && facility.getCloseTime() != null) {
                openingHours = facility.getOpenTime().format(formatter) + " - " + facility.getCloseTime().format(formatter);
            }

            VenueCardDTO dto = VenueCardDTO.builder()
                    .facilityId(facility.getId())
                    .name(facility.getName())
                    .address(facility.getAddress())
                    .imageUrl(imageUrl)
                    .sports(sports)
                    .rating(Math.round(rating * 10.0) / 10.0)
                    .reviewCount(reviewCount)
                    .openingHours(openingHours)
                    .minPricePerHour(minPrice)
                    .amenities(mockAmenities)
                    .latitude(facility.getLatitude() != null ? facility.getLatitude().doubleValue() : null)
                    .longitude(facility.getLongitude() != null ? facility.getLongitude().doubleValue() : null)
                    .build();
            venueCards.add(dto);
        }

        return venueCards;
    }
}
