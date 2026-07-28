package com.mvc.mock_project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffResponseDTO {
    private Integer staffId;
    private Integer accountId;
    private String fullName;
    private String email;
    private String phone;
    private String avatarPath;
    private Boolean isActive;
    private Integer facilityId;
    private String facilityName;
    private LocalDateTime createdAt;
}
