package com.mvc.mock_project.service;

import com.mvc.mock_project.dto.response.OwnerDashboardDTO;

public interface OwnerDashboardService {
    OwnerDashboardDTO getDashboardStats(Integer ownerId);
}
