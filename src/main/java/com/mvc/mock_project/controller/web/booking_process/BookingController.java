package com.mvc.mock_project.controller.web.booking_process;

import com.mvc.mock_project.service.FacilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/booking")
@RequiredArgsConstructor
public class BookingController {

    private final FacilityService facilityService;

    @GetMapping("/{venueId}")
    public String showBookingPage(@PathVariable Integer venueId, Model model) {
        // Just load the basic venue details for now
        model.addAttribute("venue", facilityService.getVenueById(venueId));
        return "venue/booking";
    }
}
