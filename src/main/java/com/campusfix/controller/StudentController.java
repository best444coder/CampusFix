package com.campusfix.controller;

import com.campusfix.model.*;
import com.campusfix.repository.UserRepository;
import com.campusfix.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/student")
public class StudentController {
    private final UserRepository users;
    private final ComplaintService complaintService;
    private final NotificationService notifications;

    public StudentController(UserRepository users, ComplaintService complaintService, NotificationService notifications) {
        this.users = users; this.complaintService = complaintService; this.notifications = notifications;
    }

    private User current(HttpSession s) {
        Long id = (Long)s.getAttribute("user");
        return id == null ? null : users.findById(id).orElse(null);
    }

    private boolean allowed(HttpSession s) {
        return "STUDENT".equals(s.getAttribute("role"));
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!allowed(session)) return "redirect:/login?role=student";
        User user = current(session);
        List<Complaint> list = complaintService.forStudent(user);
        model.addAttribute("user", user);
        model.addAttribute("complaints", list);
        model.addAttribute("total", list.size());
        model.addAttribute("pending", list.stream().filter(c -> "Pending".equals(c.getStatus())).count());
        model.addAttribute("progress", list.stream().filter(c -> "In Progress".equals(c.getStatus())).count());
        model.addAttribute("resolved", list.stream().filter(c -> "Resolved".equals(c.getStatus())).count());
        model.addAttribute("unread", notifications.unread(user));
        return "student/dashboard";
    }

    @GetMapping("/complaints")
    public String complaints(HttpSession session, Model model) {
        if (!allowed(session)) return "redirect:/login?role=student";
        User user = current(session);
        model.addAttribute("user", user);
        model.addAttribute("complaints", complaintService.forStudent(user));
        model.addAttribute("unread", notifications.unread(user));
        return "student/complaints";
    }

    @GetMapping("/report")
    public String report(HttpSession session, Model model) {
        if (!allowed(session)) return "redirect:/login?role=student";
        model.addAttribute("user", current(session));
        return "student/report";
    }

    @PostMapping("/report")
    public String submit(@ModelAttribute Complaint complaint, @RequestParam(required=false) MultipartFile image,
                         HttpSession session, Model model) throws IOException {
        if (!allowed(session)) return "redirect:/login?role=student";
        Complaint saved = complaintService.create(complaint, current(session), image);
        model.addAttribute("complaint", saved);
        return "student/confirmation";
    }

    @GetMapping("/complaint/{id}")
    public String details(@PathVariable String id, HttpSession session, Model model) {
        if (!allowed(session)) return "redirect:/login?role=student";
        Complaint c = complaintService.byComplaintId(id);
        if (!c.getStudent().getId().equals(current(session).getId())) return "redirect:/student/complaints";
        model.addAttribute("user", current(session));
        model.addAttribute("complaint", c);
        return "student/details";
    }

    @GetMapping("/notifications")
    public String notificationPage(HttpSession session, Model model) {
        if (!allowed(session)) return "redirect:/login?role=student";
        User user = current(session);
        model.addAttribute("user", user);
        model.addAttribute("notifications", notifications.getFor(user));
        return "student/notifications";
    }
}
