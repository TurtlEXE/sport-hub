package com.mvc.mock_project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommissionPolicyDTO {
    private Integer policyId;
    private Integer minNoticeDays;
    private String description;
    private java.time.LocalDateTime updatedAt;
}
