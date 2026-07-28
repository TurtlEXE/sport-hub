package com.mvc.mock_project.controller.web.staff;

import com.mvc.mock_project.entities.FacilitySport;
import com.mvc.mock_project.entities.Staff;
import com.mvc.mock_project.repository.FacilitySportRepository;
import com.mvc.mock_project.repository.StaffRepository;
import com.mvc.mock_project.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffController {

    private final FacilitySportRepository facilitySportRepository;
    private final StaffRepository staffRepository;

    @GetMapping({"/dashboard", "/bookings"})
    public String staffBookings(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<FacilitySport> staffFacilitySports = Collections.emptyList();
        if (userDetails != null && userDetails.getAccount() != null) {
            Optional<Staff> staffOpt = staffRepository.findByAccountId(userDetails.getAccount().getId());
            if (staffOpt.isPresent() && staffOpt.get().getFacility() != null) {
                Integer facilityId = staffOpt.get().getFacility().getId();
                staffFacilitySports = facilitySportRepository.findByFacilityIdAndIsActiveTrue(facilityId);
                if (staffFacilitySports == null || staffFacilitySports.isEmpty()) {
                    staffFacilitySports = facilitySportRepository.findByFacility_Id(facilityId);
                }
            }
        }
        model.addAttribute("ownerFacilitySports", staffFacilitySports);
        return "staff/bookings";
    }
}
