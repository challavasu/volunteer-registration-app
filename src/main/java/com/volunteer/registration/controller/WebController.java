package com.volunteer.registration.controller;

import com.volunteer.registration.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/volunteer-signup")
    public String volunteerSignup() {
        return "volunteer-signup";
    }

    @GetMapping("/my-registrations")
    public String myRegistrations() {
        return "my-registrations";
    }

    @GetMapping("/admin")
    public String admin(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        User.UserRole userRole = (User.UserRole) session.getAttribute("userRole");
        String username = (String) session.getAttribute("username");

        String roleStr = userRole != null ? userRole.toString() : "LEAD";
        model.addAttribute("currentUserRole", roleStr);
        model.addAttribute("username", username);

        return "admin";
    }

    @GetMapping("/signup")
    public String signup() {
        return "volunteer-signup";
    }

    @GetMapping("/checkin")
    public String checkin() {
        return "checkin";
    }

    @GetMapping("/login-test")
    public String loginTest() {
        return "login-test";
    }
}
