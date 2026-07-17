package com.mvc.mock_project.dto.response.facility.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminFacilityStatsDTO {
    private long totalRequests;
    private long pendingReview;
    private long approved;
    private long rejected;
    
    private long totalActiveCourts;
    private long totalActiveFacilities;
    
    private List<SportDistribution> sportDistribution;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SportDistribution {
        private String sportName;
        private long courtCount;
        private double percentage;
    }
}
