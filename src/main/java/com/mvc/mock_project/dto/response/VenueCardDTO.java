package com.mvc.mock_project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VenueCardDTO {
    private Integer facilityId;
    private String name;
    private String address;
    private String imageUrl;
    private List<String> sports; // "BASKETBALL", "PICKLEBALL"...
    private Double rating;
    private Integer reviewCount;
    private String openingHours;
    private Double minPricePerHour;
    private List<String> amenities;
    private Double latitude;
    private Double longitude;
}
