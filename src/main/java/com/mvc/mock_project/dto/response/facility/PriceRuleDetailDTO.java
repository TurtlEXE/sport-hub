package com.mvc.mock_project.dto.response.facility;

import com.mvc.mock_project.entities.enums.DayType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class PriceRuleDetailDTO {
    private Integer priceRuleId;
    private DayType dayType;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal pricePerSlot;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Boolean isActive;
}
