package com.mvc.mock_project.dto.request.facility;

import lombok.Data;
import java.util.List;

@Data
public class AssignFacilityStaffRequest {
    private List<Integer> staffIds;
}
