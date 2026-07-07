package com.mvc.mock_project.controller.web.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/dashboard")
    public String adminDashboard() {
        return "admin/dashboard";
    }
    @GetMapping("/facilities")
    public String facilityList() {
        return "admin/facility/list";
    }

    @GetMapping("/facilities/{id}")
    public String facilityDetail(@org.springframework.web.bind.annotation.PathVariable Integer id, org.springframework.ui.Model model) {
        model.addAttribute("facilityId", id);
        return "admin/facility/detail";
    }
}
