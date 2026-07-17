package com.mvc.mock_project.controller.web.booking_process;

import com.mvc.mock_project.dto.response.VenueCardDTO;
import com.mvc.mock_project.service.FacilityService;
import com.mvc.mock_project.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class BookingController {

    private final FacilityService facilityService;
    private final BookingService bookingService;

    @GetMapping("/booking/{venueId}")
    public String showBookingCalendar(@PathVariable Integer venueId, Model model) {
        VenueCardDTO venue = facilityService.getVenueById(venueId);
        if (venue == null) {
            return "redirect:/venues";
        }
        
        List<Map<String, Object>> facilitySports = bookingService.getFacilitySportsByVenue(venueId);
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn = auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());
        
        model.addAttribute("venue", venue);
        model.addAttribute("facilitySports", facilitySports);
        model.addAttribute("isLoggedIn", isLoggedIn);
        
        return "booking/booking-calendar";
    }

    @GetMapping("/api/booking/timeline")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getBookingTimeline(
            @RequestParam Integer facilitySportId,
            @RequestParam String date) {
        
        Map<String, Object> response = bookingService.getBookingTimeline(facilitySportId, date);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/booking/services")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getServices(@RequestParam Integer facilityId) {
        return ResponseEntity.ok(bookingService.getFacilityProducts(facilityId));
    }

    @GetMapping("/api/booking/vouchers")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getVouchers(@RequestParam Integer facilityId) {
        return ResponseEntity.ok(bookingService.getFacilityVouchers(facilityId));
    }
}
