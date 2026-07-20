package com.mvc.mock_project.config;

import com.mvc.mock_project.entities.enums.ActivityType;
import com.mvc.mock_project.security.CustomUserDetails;
import com.mvc.mock_project.service.UserActivityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class ActivityTrackingInterceptor implements HandlerInterceptor {

    private final UserActivityService userActivityService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String url = request.getRequestURI();
        
        // Skip tracking for static resources and api/ajax calls if needed
        // (Handled partially by WebConfig registry exclusions as well)
        if (url.startsWith("/css/") || url.startsWith("/js/") || url.startsWith("/images/") || url.startsWith("/webjars/")) {
            return true;
        }

        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        Integer userId = null;
        String username = "Anonymous";

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return true; // Skip tracking for guests
        }
        
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().contains("ADMIN"));
        if (isAdmin) {
            return true; // Skip tracking for admins
        }
        
        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            userId = userDetails.getAccount().getId();
            username = userDetails.getUsername();
        } else if (authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            username = oauth2User.getAttribute("email");
            // Note: For OAuth2, if we have a way to fetch the DB ID, we'd set userId here.
        } else {
            username = authentication.getName();
        }

        userActivityService.logActivity(userId, username, ActivityType.PAGE_VIEW, url, ipAddress, userAgent);

        return true;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        }
        return xForwardedForHeader.split(",")[0];
    }
}
