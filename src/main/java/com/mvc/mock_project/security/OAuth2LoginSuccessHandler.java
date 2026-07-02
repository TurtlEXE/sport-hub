package com.mvc.mock_project.security;

import com.mvc.mock_project.entities.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {
        
        CustomOAuth2User oauthUser = (CustomOAuth2User) authentication.getPrincipal();
        Account account = oauthUser.getAccount();
        
        if (account.getPhone() == null || account.getPhone().isEmpty()) {
            getRedirectStrategy().sendRedirect(request, response, "/auth/complete-profile");
        } else {
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }
}
