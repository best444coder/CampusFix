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

    public StudentController(
            UserRepository users,
            ComplaintService complaintService,
            NotificationService notifications) {

        this.users = users;
        this.complaintService = complaintService;
        this.notifications = notifications;
    }

    private User current(HttpSession session) {
        Object userId = session.getAttribute("user");

        if (userId == null) {
            return null;
        }

        Long id;

        if (userId instanceof Long) {
            id = (Long) userId;
        } else {
            id = Long.valueOf(userId.toString());
        }

        return users.findById(id).orElse(null);
    }

    private boolean allowed(HttpSession session) {
        Object role = session.getAttribute("role");

        return role != null
                && "STUDENT".equalsIgnoreCase(role.toString());
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {

        if (!allowed(session)) {
            return "redirect:/login?role=student";
        }

        User user = current(session);

        if (user == null) {
            session.invalidate();
            return "redirect:/login?role=student";
        }

        List<Complaint> list = complaintService.forStudent(user);

        model.addAttribute("user", user);
        model.addAttribute("complaints", list);
        model.addAttribute("total", list.size());

        model.addAttribute(
                "pending",
                list.stream()
                        .filter(c -> "Pending".equals(c.getStatus()))
                        .count()
        );

        model.addAttribute(
                "progress",
                list.stream()
                        .filter(c -> "In Progress".equals(c.getStatus()))
                        .count()
        );

        model.addAttribute(
                "resolved",
                list.stream()
                        .filter(c -> "Resolved".equals(c.getStatus()))
                        .count()
        );

        model.addAttribute("unread", notifications.unread(user));

        return "student/dashboard";
    }

    @GetMapping("/complaints")
    public String complaints(HttpSession session, Model model) {

        if (!allowed(session)) {
            return "redirect:/login?role=student";
        }

        User user = current(session);

        if (user == null) {
            session.invalidate();
            return "redirect:/login?role=student";
        }

        model.addAttribute("user", user);
        model.addAttribute(
                "complaints",
                complaintService.forStudent(user)
        );
        model.addAttribute(
                "unread",
                notifications.unread(user)
        );

        return "student/complaints";
    }

    @GetMapping("/report")
    public String report(HttpSession session, Model model) {

        if (!allowed(session)) {
            return "redirect:/login?role=student";
        }

        User user = current(session);

        if (user == null) {
            session.invalidate();
            return "redirect:/login?role=student";
        }

        model.addAttribute("user", user);

        return "student/report";
    }

    @PostMapping("/report")
    public String submit(
            @ModelAttribute Complaint complaint,
            @RequestParam(required = false) MultipartFile image,
            HttpSession session,
            Model model) throws IOException {

        if (!allowed(session)) {
            return "redirect:/login?role=student";
        }

        User user = current(session);

        if (user == null) {
            session.invalidate();
            return "redirect:/login?role=student";
        }

        Complaint saved =
                complaintService.create(complaint, user, image);

        model.addAttribute("complaint", saved);

        return "student/confirmation";
    }

    @GetMapping("/complaint/{id}")
    public String details(
            @PathVariable String id,
            HttpSession session,
            Model model) {

        if (!allowed(session)) {
            return "redirect:/login?role=student";
        }

        User user = current(session);

        if (user == null) {
            session.invalidate();
            return "redirect:/login?role=student";
        }

        Complaint complaint =
                complaintService.byComplaintId(id);

        if (complaint == null) {
            return "redirect:/student/complaints";
        }

        if (complaint.getStudent() == null
                || !complaint.getStudent()
                        .getId()
                        .equals(user.getId())) {

            return "redirect:/student/complaints";
        }

        model.addAttribute("user", user);
        model.addAttribute("complaint", complaint);

        return "student/details";
    }

    @GetMapping("/notifications")
    public String notificationPage(
            HttpSession session,
            Model model) {

        if (!allowed(session)) {
            return "redirect:/login?role=student";
        }

        User user = current(session);

        if (user == null) {
            session.invalidate();
            return "redirect:/login?role=student";
        }

        model.addAttribute("user", user);
        model.addAttribute(
                "notifications",
                notifications.getFor(user)
        );

        return "student/notifications";
    }
}
