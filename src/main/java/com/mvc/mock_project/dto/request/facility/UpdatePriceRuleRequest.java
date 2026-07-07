package com.mvc.mock_project.dto.request.facility;

import com.mvc.mock_project.entities.enums.DayType;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class UpdatePriceRuleRequest {
    private DayType dayType;
    private LocalTime startTime;
    private LocalTime endTime;
    
    @DecimalMin("0")
    private BigDecimal pricePerSlot;
    
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
}
