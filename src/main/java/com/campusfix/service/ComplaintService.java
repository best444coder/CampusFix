package com.campusfix.service;

import com.campusfix.model.Complaint;
import com.campusfix.model.User;
import com.campusfix.repository.ComplaintRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ComplaintService {
    private final ComplaintRepository repository;
    private final NotificationService notificationService;
    private final Path uploadDir = Paths.get("uploads");

    public ComplaintService(ComplaintRepository repository, NotificationService notificationService) {
        this.repository = repository;
        this.notificationService = notificationService;
        try { Files.createDirectories(uploadDir); } catch (IOException ignored) {}
    }

    public Complaint create(Complaint complaint, User student, MultipartFile image) throws IOException {
        complaint.setComplaintId("CF" + String.format("%04d", (int)(1000 + Math.random() * 9000)));
        while (repository.findByComplaintId(complaint.getComplaintId()).isPresent()) {
            complaint.setComplaintId("CF" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        }

        complaint.setStudent(student);
        complaint.setStatus("Pending");
        complaint.setDepartment(departmentFor(complaint.getCategory()));
        complaint.setSubmittedAt(LocalDateTime.now());
        complaint.setUpdatedAt(LocalDateTime.now());

        if (image != null && !image.isEmpty()) {
            String original = image.getOriginalFilename() == null ? "photo" : image.getOriginalFilename();
            String safe = UUID.randomUUID() + "_" + original.replaceAll("[^a-zA-Z0-9._-]", "_");
            Files.copy(image.getInputStream(), uploadDir.resolve(safe), StandardCopyOption.REPLACE_EXISTING);
            complaint.setImagePath("/uploads/" + safe);
        }

        Complaint saved = repository.save(complaint);
        notificationService.notify(student, "Complaint #" + saved.getComplaintId() + " has been submitted successfully.");
        return saved;
    }

    public List<Complaint> forStudent(User student) {
        return repository.findByStudentOrderBySubmittedAtDesc(student);
    }

    public Complaint byComplaintId(String id) {
        return repository.findByComplaintId(id).orElseThrow();
    }

    public List<Complaint> all() {
        return repository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "submittedAt"));
    }

    public void update(Complaint complaint, String department, String status, String notes) {
        complaint.setDepartment(department);
        complaint.setStatus(status);
        complaint.setResolutionNotes(notes);
        complaint.setUpdatedAt(LocalDateTime.now());
        repository.save(complaint);

        String message = "Complaint #" + complaint.getComplaintId() + " is now " + status + ".";
        if (department != null && !department.isBlank()) {
            message = "Complaint #" + complaint.getComplaintId() + " has been assigned to " + department + ".";
            if (!"Pending".equals(status)) {
                message += " Status: " + status + ".";
            }
        }
        if ("Resolved".equals(status)) {
            message = "Complaint #" + complaint.getComplaintId() + " has been Resolved.";
        }
        notificationService.notify(complaint.getStudent(), message);
    }

    private String departmentFor(String category) {
        return switch (category) {
            case "Electrical" -> "Electrical Maintenance";
            case "Plumbing", "Water Supply", "Washroom / Sanitation" -> "Civil & Maintenance";
            case "Wi-Fi / Internet", "Laboratory", "Projector / AV Equipment" -> "IT Support";
            case "Furniture", "Classroom" -> "Facilities";
            default -> "General Administration";
        };
    }
}
