package com.mvc.mock_project.controller.web.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminProductCategoryWebController {

    @GetMapping("/product-categories")
    public String showProductCategoryPage(Model model) {
        model.addAttribute("pageTitle", "Product Category");
        return "admin/product-category";
    }
}
