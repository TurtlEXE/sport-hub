package com.mvc.mock_project.controller.web.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminSportController {

    @GetMapping("/sports")
    public String showSportManagementPage(Model model) {
        model.addAttribute("pageTitle", "Sport Management");
        return "admin/sport-management";
    }
}
