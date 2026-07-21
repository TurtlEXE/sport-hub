package com.mvc.mock_project.service;

import com.mvc.mock_project.entities.UserActivityLog;
import com.mvc.mock_project.entities.enums.ActivityType;

import java.util.List;

public interface UserActivityService {
    void logActivity(Integer userId, String username, ActivityType activityType, String url, String ipAddress, String userAgent);
    List<UserActivityLog> getAllActivities();
    org.springframework.data.domain.Page<UserActivityLog> getFilteredActivities(String search, java.time.LocalDate date, com.mvc.mock_project.entities.enums.Role role, org.springframework.data.domain.Pageable pageable);
    void cleanupOldLogs();
}
