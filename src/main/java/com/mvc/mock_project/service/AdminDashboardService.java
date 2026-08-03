package com.mvc.mock_project.service;

import com.mvc.mock_project.dto.response.AdminDashboardDTO;
import com.mvc.mock_project.dto.response.FacilityBookingDetailDTO;

import java.util.List;

public interface AdminDashboardService {
    AdminDashboardDTO getDashboardStats();
    List<FacilityBookingDetailDTO> getFacilityBookingsDetail(Long facilityId);
}
