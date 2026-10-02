package com.campusfix.controller;

import com.campusfix.model.User;
import com.campusfix.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {
    private final UserRepository users;

    public AuthController(UserRepository users) {
        this.users = users;
    }

    @GetMapping("/")
    public String home() { return "landing"; }

    @GetMapping("/login")
    public String login(@RequestParam(defaultValue="student") String role, Model model) {
        model.addAttribute("role", role);
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String email, @RequestParam String password,
                          @RequestParam String role, HttpSession session, Model model) {
        User user = users.findByEmail(email).orElse(null);
        if (user != null && user.getPassword().equals(password) && user.getRole().equalsIgnoreCase(role)) {
            session.setAttribute("user", user.getId());
            session.setAttribute("role", user.getRole());
            return "ADMIN".equals(user.getRole()) ? "redirect:/admin/dashboard" : "redirect:/student/dashboard";
        }
        model.addAttribute("role", role);
        model.addAttribute("error", "Invalid email, password, or role.");
        return "login";
    }

    @GetMapping("/register")
    public String register() { return "register"; }

    @PostMapping("/register")
    public String register(@RequestParam String name, @RequestParam String email,
                           @RequestParam String password, HttpSession session, Model model) {
        if (users.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Email already registered.");
            return "register";
        }
        User user = users.save(User.builder().name(name).email(email).password(password).role("STUDENT").build());
        session.setAttribute("user", user.getId());
        session.setAttribute("role", "STUDENT");
        return "redirect:/student/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
