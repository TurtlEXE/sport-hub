package com.mvc.mock_project.dto.response.facility;

import com.mvc.mock_project.entities.enums.ApprovalStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminFacilityReviewDTO {
    private Integer facilityId;
    private String name;
    private String address;
    private String ownerName;
    private String ownerEmail;
    private String ownerPhone;
    private String businessName;
    private ApprovalStatus approvalStatus;
    private LocalDateTime createdAt;
    private String thumbnailUrl;
    private Integer totalCourts;
}
