package com.mvc.mock_project.service.impl;

import com.mvc.mock_project.entities.Account;
import com.mvc.mock_project.entities.UserActivityLog;
import com.mvc.mock_project.entities.enums.ActivityType;
import com.mvc.mock_project.repository.UserActivityLogRepository;
import com.mvc.mock_project.service.UserActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserActivityServiceImpl implements UserActivityService {

    private final UserActivityLogRepository userActivityLogRepository;

    @Async
    @Override
    public void logActivity(Integer userId, String username, ActivityType activityType, String url, String ipAddress, String userAgent) {
        try {
            Account account = null;
            if (userId != null) {
                account = new Account();
                account.setId(userId);
            }
            
            UserActivityLog logEntry = UserActivityLog.builder()
                    .account(account)
                    .username(username)
                    .activityType(activityType)
                    .url(url)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .timestamp(LocalDateTime.now())
                    .build();
            userActivityLogRepository.save(logEntry);
        } catch (Exception e) {
            log.error("Failed to save user activity log: {}", e.getMessage());
        }
    }

    @Override
    public List<UserActivityLog> getAllActivities() {
        return userActivityLogRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp"));
    }

    @Override
    public org.springframework.data.domain.Page<UserActivityLog> getFilteredActivities(String search, java.time.LocalDate date, com.mvc.mock_project.entities.enums.Role role, org.springframework.data.domain.Pageable pageable) {
        String keyword = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        if (date != null) {
            startDate = date.atStartOfDay();
            endDate = date.plusDays(1).atStartOfDay().minusNanos(1);
        }
        return userActivityLogRepository.findFilteredActivities(keyword, startDate, endDate, role, pageable);
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * ?") // Runs every day at midnight
    @Override
    public void cleanupOldLogs() {
        log.info("Starting cleanup of old user activity logs...");
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        userActivityLogRepository.deleteLogsOlderThan(threeDaysAgo);
        log.info("Finished cleanup of user activity logs older than {}", threeDaysAgo);
    }
}
