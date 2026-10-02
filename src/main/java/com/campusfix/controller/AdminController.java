package com.campusfix.controller;

import com.campusfix.model.*;
import com.campusfix.repository.UserRepository;
import com.campusfix.service.ComplaintService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserRepository users;
    private final ComplaintService service;

    public AdminController(UserRepository users, ComplaintService service) {
        this.users = users; this.service = service;
    }

    private boolean allowed(HttpSession s) { return "ADMIN".equals(s.getAttribute("role")); }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!allowed(session)) return "redirect:/login?role=admin";
        List<Complaint> all = service.all();
        model.addAttribute("user", current(session));
        model.addAttribute("complaints", all);
        model.addAttribute("total", all.size());
        model.addAttribute("pending", all.stream().filter(c -> "Pending".equals(c.getStatus())).count());
        model.addAttribute("progress", all.stream().filter(c -> "In Progress".equals(c.getStatus())).count());
        model.addAttribute("resolved", all.stream().filter(c -> "Resolved".equals(c.getStatus())).count());

        Map<String, Long> category = all.stream().collect(Collectors.groupingBy(Complaint::getCategory, LinkedHashMap::new, Collectors.counting()));
        Map<String, Long> status = all.stream().collect(Collectors.groupingBy(Complaint::getStatus, LinkedHashMap::new, Collectors.counting()));
        model.addAttribute("categoryJson", toJson(category));
        model.addAttribute("statusJson", toJson(status));
        return "admin/dashboard";
    }

    @GetMapping("/complaints")
    public String complaints(@RequestParam(required=false) String search,
                             @RequestParam(required=false) String category,
                             @RequestParam(required=false) String department,
                             @RequestParam(required=false) String status,
                             HttpSession session, Model model) {
        if (!allowed(session)) return "redirect:/login?role=admin";
        List<Complaint> all = service.all();
        if (search != null && !search.isBlank()) {
            String q = search.toLowerCase();
            all = all.stream().filter(c -> c.getComplaintId().toLowerCase().contains(q)
                    || c.getTitle().toLowerCase().contains(q)
                    || c.getBuilding().toLowerCase().contains(q)).toList();
        }
        if (category != null && !category.isBlank()) all = all.stream().filter(c -> category.equals(c.getCategory())).toList();
        if (department != null && !department.isBlank()) all = all.stream().filter(c -> department.equals(c.getDepartment())).toList();
        if (status != null && !status.isBlank()) all = all.stream().filter(c -> status.equals(c.getStatus())).toList();

        model.addAttribute("user", current(session));
        model.addAttribute("complaints", all);
        model.addAttribute("allComplaints", service.all());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedDepartment", department);
        model.addAttribute("selectedStatus", status);
        return "admin/complaints";
    }

    @GetMapping("/complaint/{id}")
    public String details(@PathVariable String id, HttpSession session, Model model) {
        if (!allowed(session)) return "redirect:/login?role=admin";
        model.addAttribute("user", current(session));
        model.addAttribute("complaint", service.byComplaintId(id));
        return "admin/details";
    }

    @PostMapping("/complaint/{id}/update")
    public String update(@PathVariable String id, @RequestParam String department,
                         @RequestParam String status, @RequestParam(required=false) String notes,
                         HttpSession session) {
        if (!allowed(session)) return "redirect:/login?role=admin";
        service.update(service.byComplaintId(id), department, status, notes);
        return "redirect:/admin/complaint/" + id;
    }

    private User current(HttpSession s) {
        Long id = (Long)s.getAttribute("user");
        return id == null ? null : users.findById(id).orElse(null);
    }

    private String toJson(Map<String, Long> map) {
        return map.entrySet().stream()
                .map(e -> "\"" + e.getKey().replace("\"","\\\"") + "\":" + e.getValue())
                .collect(Collectors.joining(",", "{", "}"));
    }
}
