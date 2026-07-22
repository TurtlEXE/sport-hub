package com.mvc.mock_project.controller.api.booking;

import com.mvc.mock_project.entities.Court;
import com.mvc.mock_project.entities.CourtSlotBooking;
import com.mvc.mock_project.entities.FacilitySport;
import com.mvc.mock_project.repository.CourtRepository;
import com.mvc.mock_project.repository.CourtSlotBookingRepository;
import com.mvc.mock_project.repository.FacilitySportRepository;
import com.mvc.mock_project.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/owner/booking")
@RequiredArgsConstructor
public class OwnerBookingApiController {

    private final FacilitySportRepository facilitySportRepository;
    private final CourtRepository courtRepository;
    private final CourtSlotBookingRepository courtSlotBookingRepository;

    @GetMapping("/timeline")
    public ResponseEntity<Map<String, Object>> getOwnerBookingTimeline(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Integer facilitySportId,
            @RequestParam String date) {

        Integer ownerId = userDetails.getAccount().getId();
        LocalDate bookingDate = LocalDate.parse(date);

        // Verify that this facilitySport belongs to the owner
        FacilitySport fs = facilitySportRepository.findByIdAndFacility_Owner_Id(facilitySportId, ownerId)
                .orElseThrow(() -> new RuntimeException("FacilitySport not found or access denied"));

        Map<String, Object> response = new HashMap<>();
        response.put("date", date);
        response.put("slotDurationMinutes", fs.getSlotStepMinutes());

        List<Court> courts = courtRepository.findByFacilitySportIdAndIsActiveTrue(facilitySportId);
        List<Integer> courtIds = courts.stream().map(Court::getId).collect(Collectors.toList());

        List<CourtSlotBooking> bookedSlots = new ArrayList<>();
        if (!courtIds.isEmpty()) {
            bookedSlots = courtSlotBookingRepository.findActiveHoldsByCourtIdsAndDate(courtIds, bookingDate);
        }

        List<Map<String, Object>> courtsData = new ArrayList<>();
        for (Court court : courts) {
            Map<String, Object> cMap = new HashMap<>();
            cMap.put("courtId", court.getId());
            cMap.put("courtName", court.getCourtName());

            List<Map<String, Object>> slots = generateSlots(
                    fs.getFacility().getOpenTime(),
                    fs.getFacility().getCloseTime(),
                    fs.getSlotStepMinutes(),
                    court.getId(),
                    bookedSlots
            );
            cMap.put("slots", slots);
            courtsData.add(cMap);
        }

        response.put("courts", courtsData);
        return ResponseEntity.ok(response);
    }

    private List<Map<String, Object>> generateSlots(
            LocalTime openTime, LocalTime closeTime, int stepMins,
            Integer courtId, List<CourtSlotBooking> bookedSlots) {

        List<Map<String, Object>> slots = new ArrayList<>();
        int slotIndex = 0;

        LocalTime curr = openTime;
        while (curr.isBefore(closeTime)) {
            LocalTime next = curr.plusMinutes(stepMins);
            if (next.isAfter(closeTime) || (next.isBefore(curr) && next.equals(LocalTime.MIDNIGHT))) {
                if (next.equals(LocalTime.MIDNIGHT)) {
                    next = LocalTime.MAX;
                } else {
                    next = closeTime;
                }
            }

            Map<String, Object> slot = new HashMap<>();
            slot.put("slotIndex", slotIndex);
            slot.put("startTime", formatTime(curr));
            slot.put("endTime", formatTime(next));

            String status = "AVAILABLE";
            String bookerName = "";
            String bookerPhone = "";

            for (CourtSlotBooking bs : bookedSlots) {
                if (bs.getCourt().getId().equals(courtId)) {
                    if (curr.isBefore(bs.getEndTime()) && next.isAfter(bs.getStartTime())) {
                        if (bs.getBookingSlot() != null && bs.getBookingSlot().getBooking() != null) {
                            com.mvc.mock_project.entities.Booking booking = bs.getBookingSlot().getBooking();
                            if (booking.getBookingStatus() == com.mvc.mock_project.entities.enums.BookingStatus.PENDING) {
                                status = "HOLD";
                            } else {
                                status = "BOOKED";
                            }
                            if (booking.getGuest() != null) {
                                bookerName = booking.getGuest().getGuestName();
                                bookerPhone = booking.getGuest().getPhone();
                            } else if (booking.getAccount() != null) {
                                bookerName = booking.getAccount().getFullName();
                                bookerPhone = booking.getAccount().getPhone();
                            }
                        } else {
                            status = "HOLD";
                        }
                        break;
                    }
                }
            }

            slot.put("status", status);
            slot.put("bookerName", bookerName);
            slot.put("bookerPhone", bookerPhone);

            slots.add(slot);
            slotIndex++;
            curr = next;
            if (curr.equals(LocalTime.MAX) || curr.equals(closeTime)) {
                break;
            }
        }

        return slots;
    }

    private String formatTime(LocalTime time) {
        if (time.equals(LocalTime.MAX)) return "23:59";
        return String.format("%02d:%02d", time.getHour(), time.getMinute());
    }
}
