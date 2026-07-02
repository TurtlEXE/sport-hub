package com.mvc.mock_project.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mvc.mock_project.controller.AuthController;

@ControllerAdvice(assignableTypes = {AuthController.class})
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public String handleEmailAlreadyExistsException(EmailAlreadyExistsException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMsg", ex.getMessage());
        return "redirect:/auth/register";
    }

    @ExceptionHandler(InvalidOtpException.class)
    public String handleInvalidOtpException(InvalidOtpException ex, Model model) {
        model.addAttribute("errorMsg", ex.getMessage());
        return "auth/verify-otp"; // For both verify and reset, simple fallback
    }

    @ExceptionHandler(OtpExpiredException.class)
    public String handleOtpExpiredException(OtpExpiredException ex, Model model) {
        model.addAttribute("errorMsg", ex.getMessage());
        return "auth/verify-otp"; 
    }

    @ExceptionHandler(AccountNotActiveException.class)
    public String handleAccountNotActiveException(AccountNotActiveException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMsg", ex.getMessage());
        return "redirect:/auth/login";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMsg", "Đã xảy ra lỗi: " + ex.getMessage());
        return "redirect:/auth/login";
    }
}
