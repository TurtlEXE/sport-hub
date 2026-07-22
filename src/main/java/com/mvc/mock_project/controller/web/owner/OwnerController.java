package com.mvc.mock_project.controller.web.owner;

import com.mvc.mock_project.entities.FacilitySport;
import com.mvc.mock_project.repository.FacilitySportRepository;
import com.mvc.mock_project.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/owner")
@RequiredArgsConstructor
public class OwnerController {

    private final FacilitySportRepository facilitySportRepository;

    @GetMapping("/dashboard")
    public String ownerDashboard() {
        return "owner/dashboard";
    }

    @GetMapping("/bookings")
    public String myBookings(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null && userDetails.getAccount() != null) {
            List<FacilitySport> ownerFacilitySports = facilitySportRepository.findByFacility_Owner_IdAndIsActiveTrue(userDetails.getAccount().getId());
            model.addAttribute("ownerFacilitySports", ownerFacilitySports);
        }
        return "owner/bookings";
    }
    @GetMapping("/facilities")
    public String myFacilities() {
        return "owner/facility/list";
    }

    @GetMapping("/facilities/create")
    public String createFacility() {
        return "owner/facility/create";
    }

    @GetMapping("/facilities/{id}")
    public String facilityDetail(@org.springframework.web.bind.annotation.PathVariable Integer id, org.springframework.ui.Model model) {
        model.addAttribute("facilityId", id);
        return "owner/facility/detail";
    }
}
