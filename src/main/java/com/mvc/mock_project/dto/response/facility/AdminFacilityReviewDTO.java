package com.mvc.mock_project.dto.response.facility;

import com.mvc.mock_project.entities.enums.ApprovalStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminFacilityReviewDTO {
    private Integer facilityId;
    private String name;
    private String address;
    private String fullAddress;
    private String ownerName;
    private String ownerEmail;
    private String ownerPhone;
    private String businessName;
    private ApprovalStatus approvalStatus;
    private LocalDateTime createdAt;
    private String thumbnailUrl;
    private Integer totalCourts;
    
    private String description;
    private java.math.BigDecimal latitude;
    private java.math.BigDecimal longitude;
    private java.time.LocalTime openTime;
    private java.time.LocalTime closeTime;
    private java.util.List<FacilityImageDTO> galleryImages;
    private java.util.List<FacilitySportDTO> sports;
}
