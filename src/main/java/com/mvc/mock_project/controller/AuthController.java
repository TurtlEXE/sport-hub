package com.mvc.mock_project.controller;

import com.mvc.mock_project.dto.request.*;
import com.mvc.mock_project.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    public String showLoginPage(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            return "redirect:/"; // Already logged in
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                                  BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/register";
        }
        try {
            authService.registerCustomer(request);
            redirectAttributes.addAttribute("email", request.getEmail());
            redirectAttributes.addFlashAttribute("successMsg", "msg.success.register");
            return "redirect:/auth/verify";
        } catch (Exception ex) {
            result.rejectValue("email", "error.registerRequest", ex.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/register/owner")
    public String showOwnerRegisterPage(Model model) {
        model.addAttribute("ownerRegisterRequest", new OwnerRegisterRequest());
        return "auth/register-owner";
    }

    @PostMapping("/register/owner")
    public String processOwnerRegister(@Valid @ModelAttribute("ownerRegisterRequest") OwnerRegisterRequest request,
                                       BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/register-owner";
        }
        try {
            authService.registerOwner(request);
            redirectAttributes.addAttribute("email", request.getEmail());
            redirectAttributes.addFlashAttribute("successMsg", "msg.success.register");
            return "redirect:/auth/verify";
        } catch (Exception ex) {
            result.rejectValue("email", "error.ownerRegisterRequest", ex.getMessage());
            return "auth/register-owner";
        }
    }

    @GetMapping("/verify")
    public String showVerifyOtpPage(@RequestParam(value = "email", required = false) String email, Model model) {
        if (email == null || email.isEmpty()) {
            return "redirect:/auth/login";
        }
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail(email);
        model.addAttribute("verifyRequest", request);
        return "auth/verify-otp";
    }

    @PostMapping("/verify")
    public String processVerifyOtp(@Valid @ModelAttribute("verifyRequest") VerifyOtpRequest request,
                                   BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/verify-otp";
        }
        try {
            authService.verifyEmailOtp(request);
            redirectAttributes.addFlashAttribute("successMsg", "msg.success.verify");
            return "redirect:/auth/login";
        } catch (Exception ex) {
            result.rejectValue("otp", "error.verifyRequest", ex.getMessage());
            return "auth/verify-otp";
        }
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordPage(Model model) {
        model.addAttribute("forgotRequest", new ForgotPasswordRequest());
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@Valid @ModelAttribute("forgotRequest") ForgotPasswordRequest request,
                                        BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/forgot-password";
        }
        authService.forgotPassword(request.getEmail());
        redirectAttributes.addAttribute("email", request.getEmail());
        redirectAttributes.addFlashAttribute("successMsg", "msg.success.forgot");
        return "redirect:/auth/reset-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordPage(@RequestParam(value = "email", required = false) String email, Model model) {
        if (email == null || email.isEmpty()) {
            return "redirect:/auth/login";
        }
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail(email);
        model.addAttribute("resetRequest", request);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@Valid @ModelAttribute("resetRequest") ResetPasswordRequest request,
                                       BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/reset-password";
        }
        try {
            authService.resetPassword(request);
            redirectAttributes.addFlashAttribute("successMsg", "msg.success.reset");
            return "redirect:/auth/login";
        } catch (Exception ex) {
            result.rejectValue("otp", "error.resetRequest", ex.getMessage());
            return "auth/reset-password";
        }
    }

    @GetMapping("/complete-profile")
    public String showCompleteProfilePage(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/auth/login";
        }
        model.addAttribute("completeProfileRequest", new CompleteProfileRequest());
        return "auth/complete-profile";
    }

    @PostMapping("/complete-profile")
    public String processCompleteProfile(@Valid @ModelAttribute("completeProfileRequest") CompleteProfileRequest request,
                                         BindingResult result, Authentication authentication,
                                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/complete-profile";
        }
        
        // Wait, authentication might be OAuth2User
        // Need to extract email safely
        String email = "";
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof com.mvc.mock_project.security.CustomOAuth2User) {
                email = ((com.mvc.mock_project.security.CustomOAuth2User) principal).getAccount().getEmail();
            } else if (principal instanceof com.mvc.mock_project.security.CustomUserDetails) {
                email = ((com.mvc.mock_project.security.CustomUserDetails) principal).getUsername();
            } else {
                email = authentication.getName(); // Fallback
            }
        } else {
            return "redirect:/auth/login";
        }

        try {
            authService.completeProfile(email, request);
            redirectAttributes.addFlashAttribute("successMsg", "msg.success.profile");
            return "redirect:/"; // To home page
        } catch (Exception ex) {
            result.rejectValue("phone", "error.completeProfileRequest", ex.getMessage());
            return "auth/complete-profile";
        }
    }
}
