package com.mvc.mock_project.controller.web.customer;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/customer")
public class CustomerBookingController {

    @GetMapping("/bookings")
    public String myBookings() {
        return "customer/bookings";
    }
}
