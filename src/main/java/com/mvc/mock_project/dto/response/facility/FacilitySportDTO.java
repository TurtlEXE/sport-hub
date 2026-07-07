package com.mvc.mock_project.dto.response.facility;

import lombok.Data;

import java.util.List;

@Data
public class FacilitySportDTO {
    private Integer facilitySportId;
    private Integer sportId;
    private String sportName;
    private String sportCode;
    private Integer minDurationMinutes;
    private Integer slotStepMinutes;
    private Boolean isActive;
    private Integer courtCount;
    private List<CourtDTO> courts;
    private List<PriceRuleDetailDTO> priceRules;
    private List<String> slotBoundaries;
}
