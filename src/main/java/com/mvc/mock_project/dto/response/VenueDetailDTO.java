package com.mvc.mock_project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class VenueDetailDTO extends VenueCardDTO {
    private Map<String, List<ProductDTO>> groupedServices;
    private Map<String, List<PriceRuleDTO>> groupedPriceRules;
    private Integer slotDurationMinutes;
    private List<String> galleryImages;
    private List<ReviewDTO> reviews;
}
