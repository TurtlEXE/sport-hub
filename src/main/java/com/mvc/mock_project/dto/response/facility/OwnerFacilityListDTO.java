package com.mvc.mock_project.dto.response.facility;

import com.mvc.mock_project.entities.enums.ApprovalStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OwnerFacilityListDTO {
    private Integer facilityId;
    private String name;
    private String address;
    private String fullAddress;
    private String thumbnailUrl;
    private ApprovalStatus approvalStatus;
    private Boolean isActive;
    private Integer totalCourts;
    private Integer totalSports;
    private LocalDateTime createdAt;
}
