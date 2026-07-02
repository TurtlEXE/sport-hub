package com.mvc.mock_project.controller.web;

import com.mvc.mock_project.service.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;

    @GetMapping("/venues")
    public String showVenues(Model model) {
        model.addAttribute("sports", venueService.getAllActiveSports());
        model.addAttribute("venues", venueService.getAllActiveVenues());
        return "venues";
    }
}
