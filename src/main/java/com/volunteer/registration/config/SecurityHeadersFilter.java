package com.volunteer.registration.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filter to add additional security headers to all HTTP responses.
 * Provides defense-in-depth for common web vulnerabilities.
 */
@Component
public class SecurityHeadersFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (response instanceof HttpServletResponse) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            // Prevent MIME type sniffing
            httpResponse.setHeader("X-Content-Type-Options", "nosniff");

            // Prevent clickjacking attacks
            httpResponse.setHeader("X-Frame-Options", "DENY");

            // Enable XSS protection
            httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

            // HTTPS Strict Transport Security (only over HTTPS)
            // Set to max-age of 31536000 (1 year), includeSubDomains for subdomains
            httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

            // Content Security Policy - strict policy to prevent inline scripts and external resources
            httpResponse.setHeader("Content-Security-Policy",
                "default-src 'self'; " +
                "script-src 'self' cdn.quilljs.com cdnjs.cloudflare.com; " +
                "style-src 'self' cdnjs.cloudflare.com fonts.googleapis.com fonts.gstatic.com; " +
                "font-src 'self' cdnjs.cloudflare.com fonts.gstatic.com; " +
                "img-src 'self' data:; " +
                "connect-src 'self'; " +
                "frame-ancestors 'self'; " +
                "base-uri 'self'; " +
                "form-action 'self'");

            // Referrer Policy - control how much referrer information is shared
            httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

            // Permissions Policy - disable unnecessary features
            httpResponse.setHeader("Permissions-Policy",
                "geolocation=(), microphone=(), camera=(), payment=(), usb=(), magnetometer=(), gyroscope=(), accelerometer=()");

            // Remove server identification headers
            httpResponse.setHeader("Server", "");

            // Prevent caching of sensitive content
            httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, private");
            httpResponse.setHeader("Pragma", "no-cache");
            httpResponse.setHeader("Expires", "0");
        }

        chain.doFilter(request, response);
    }
}
