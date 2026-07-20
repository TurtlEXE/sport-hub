package com.mvc.mock_project.controller.web;

import com.mvc.mock_project.service.UserActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mvc.mock_project.entities.UserActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/tracking")
@RequiredArgsConstructor
public class AdminTrackingController {

    private final UserActivityService userActivityService;

    @GetMapping
    public String viewTrackingDashboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) com.mvc.mock_project.entities.enums.Role role,
            @org.springframework.web.bind.annotation.RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<UserActivityLog> activityPage = userActivityService.getFilteredActivities(search, date, role, pageable);
        
        model.addAttribute("activityPage", activityPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", activityPage.getTotalPages());
        model.addAttribute("totalItems", activityPage.getTotalElements());
        model.addAttribute("search", search);
        model.addAttribute("date", date);
        model.addAttribute("role", role);
        
        if ("XMLHttpRequest".equals(requestedWith)) {
            return "admin/tracking-dashboard :: trackingTable";
        }
        
        return "admin/tracking-dashboard";
    }
}
