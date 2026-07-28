package com.mvc.mock_project.controller.web.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminCommissionWebController {

    @GetMapping("/commission")
    public String showCommissionManagementPage(Model model) {
        model.addAttribute("pageTitle", "Commission Policy");
        return "admin/commission-policy";
    }
}
