package com.mvc.mock_project.task;

import com.mvc.mock_project.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingCleanupTask {

    private final BookingService bookingService;

    // Runs every 30 seconds
    @Scheduled(fixedRate = 30000)
    public void cleanupExpiredBookings() {
        try {
            bookingService.cancelExpiredBookings();
        } catch (Exception e) {
            log.error("Error during expired bookings cleanup", e);
        }
    }
}
