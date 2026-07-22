package com.mvc.mock_project.controller.web.owner;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/owner")
public class OwnerController {

    @GetMapping("/dashboard")
    public String ownerDashboard() {
        return "owner/dashboard";
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

    @GetMapping("/staff")
    public String staffList() {
        return "owner/staff/list";
    }


}
