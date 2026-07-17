package com.mvc.mock_project.dto.response.facility;

import com.mvc.mock_project.entities.enums.ApprovalStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
public class OwnerFacilityDetailDTO {
    private Integer facilityId;
    private String name;
    private String address;
    private String province;
    private String district;
    private String ward;
    private String fullAddress;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String description;
    private LocalTime openTime;
    private LocalTime closeTime;
    private ApprovalStatus approvalStatus;
    private String rejectionReason;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private String thumbnailUrl;
    private List<FacilityImageDTO> galleryImages;
    private List<FacilitySportDTO> sports;
}
