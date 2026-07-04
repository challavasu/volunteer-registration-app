package com.volunteer.registration.controller;

import com.volunteer.registration.model.User;
import com.volunteer.registration.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        Optional<User> userOpt = authService.authenticate(username, password);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("userRole", user.getRole());

            setupSecurityContext(user, session);
            System.out.println("✓ Login successful for user: " + username);
            return "redirect:/admin";
        } else {
            System.out.println("✗ Login failed for user: " + username);
            model.addAttribute("error", "Invalid username or password");
            return "login";
        }
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<Map<String, Object>> apiLogin(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session) {

        Optional<User> userOpt = authService.authenticate(username, password);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("userRole", user.getRole());

            setupSecurityContext(user, session);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("redirectUrl", "/admin");
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Invalid username or password");
            return ResponseEntity.status(401).body(response);
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/api/debug/check-admin")
    public ResponseEntity<String> checkAdminUser() {
        Optional<User> admin = authService.getUserByUsername("admin");
        if (admin.isPresent()) {
            User user = admin.get();
            return ResponseEntity.ok("✓ Admin user EXISTS\n" +
                    "ID: " + user.getId() + "\n" +
                    "Username: " + user.getUsername() + "\n" +
                    "Role: " + user.getRole() + "\n" +
                    "Active: " + user.isActive() + "\n" +
                    "\nTest login with:\n" +
                    "Username: admin\n" +
                    "Password: admin123");
        } else {
            return ResponseEntity.status(404).body("✗ Admin user NOT FOUND in database");
        }
    }

    private void setupSecurityContext(User user, HttpSession session) {
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().toString()));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities);
        authentication.setDetails(user);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
    }
}
