package com.mvc.mock_project.controller.web.profile;

import com.mvc.mock_project.entities.Account;
import com.mvc.mock_project.entities.enums.Role;
import com.mvc.mock_project.security.CustomOAuth2User;
import com.mvc.mock_project.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @GetMapping("/settings")
    public String settings(Authentication authentication) {
        Account account = extractAccount(authentication);

        if (account.getRole() == Role.CUSTOMER) {
            return "profile/settings";
        }

        // OWNER, ADMIN, STAFF → dashboard layout
        return "profile/dashboard-settings";
    }

    private Account extractAccount(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getAccount();
        } else if (principal instanceof CustomOAuth2User) {
            return ((CustomOAuth2User) principal).getAccount();
        }
        throw new RuntimeException("msg.error.account_not_found");
    }
}
