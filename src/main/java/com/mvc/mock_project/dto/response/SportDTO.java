package com.mvc.mock_project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SportDTO {
    private Integer sportId;
    private String sportCode;
    private String sportName;
    private String iconPath;
    private Integer defaultMinDurationMinutes;
    private Integer defaultSlotStepMinutes;
    private Boolean isActive;
}
