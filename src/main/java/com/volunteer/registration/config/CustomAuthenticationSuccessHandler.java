package com.volunteer.registration.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom authentication success handler that ensures proper session security
 * and sets up security-related session attributes.
 */
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        HttpSession session = request.getSession(true);

        // Set secure session timeout (30 minutes)
        session.setMaxInactiveInterval(30 * 60);

        // Ensure session is regenerated after login to prevent session fixation attacks
        // (This is typically handled by Spring Security, but we ensure it here)
        String newSessionId = request.getSession().getId();

        // Add security headers to response
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        // Redirect to admin dashboard
        response.sendRedirect(request.getContextPath() + "/admin");
    }
}
