package com.mvc.mock_project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceRuleDTO {
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal pricePerSlot;
    private String dayType;
    private String sportName;
}
