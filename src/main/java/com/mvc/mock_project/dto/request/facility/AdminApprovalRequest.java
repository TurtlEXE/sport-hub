package com.mvc.mock_project.dto.request.facility;

import com.mvc.mock_project.entities.enums.ApprovalStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminApprovalRequest {
    @NotNull
    private ApprovalStatus status;
    
    private String rejectionReason;
}
