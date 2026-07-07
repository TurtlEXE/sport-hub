package com.mvc.mock_project.dto.request.facility;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class CreateFacilityRequest {
    @NotBlank
    private String name;
    
    @NotBlank
    private String address;
    
    @NotBlank
    private String province;
    
    @NotBlank
    private String district;
    
    @NotBlank
    private String ward;
    
    private BigDecimal latitude;
    private BigDecimal longitude;
    
    private String description;
    
    @NotNull
    private LocalTime openTime;
    
    @NotNull
    private LocalTime closeTime;
}
