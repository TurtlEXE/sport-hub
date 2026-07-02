package com.mvc.mock_project.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/login")
    public String showLoginPage(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            return "redirect:/"; // Already logged in
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "auth/register";
    }

    @GetMapping("/register/owner")
    public String showOwnerRegisterPage() {
        return "auth/register-owner";
    }

    @GetMapping("/verify")
    public String showVerifyOtpPage(@RequestParam(value = "email", required = false) String email, Model model) {
        if (email == null || email.isEmpty()) {
            return "redirect:/auth/login";
        }
        model.addAttribute("email", email);
        return "auth/verify-otp";
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() {
        return "auth/forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordPage(@RequestParam(value = "email", required = false) String email, Model model) {
        if (email == null || email.isEmpty()) {
            return "redirect:/auth/login";
        }
        model.addAttribute("email", email);
        return "auth/reset-password";
    }

    @GetMapping("/complete-profile")
    public String showCompleteProfilePage(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/auth/login";
        }
        return "auth/complete-profile";
    }
}
