package com.mvc.mock_project.dto.request.facility;

import com.mvc.mock_project.entities.enums.DayType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreatePriceRuleRequest {
    @NotNull
    private Integer facilitySportId;
    
    @NotNull
    private DayType dayType;
    
    @NotNull
    private LocalTime startTime;
    
    @NotNull
    private LocalTime endTime;
    
    @NotNull
    @DecimalMin("0")
    private BigDecimal pricePerSlot;
    
    @NotNull
    private LocalDate effectiveFrom;
    
    private LocalDate effectiveTo;
}
