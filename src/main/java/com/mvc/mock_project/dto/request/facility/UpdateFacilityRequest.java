package com.mvc.mock_project.dto.request.facility;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class UpdateFacilityRequest {
    private String name;
    private String address;
    private String province;
    private String district;
    private String ward;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String description;
    private LocalTime openTime;
    private LocalTime closeTime;
}
