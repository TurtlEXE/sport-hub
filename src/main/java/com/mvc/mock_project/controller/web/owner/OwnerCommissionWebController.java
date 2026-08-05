package com.mvc.mock_project.controller.web.owner;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/owner")
public class OwnerCommissionWebController {

    @GetMapping("/commission")
    public String showCommissionPolicyPage(Model model) {
        model.addAttribute("pageTitle", "Platform Commission Policy");
        return "owner/commission-policy";
    }
}
