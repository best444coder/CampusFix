package com.campusfix.config;

import com.campusfix.model.*;
import com.campusfix.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.LocalDateTime;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner init(UserRepository users, ComplaintRepository complaints) {
        return args -> {
            User student = users.findByEmail("student@campusfix.com").orElseGet(() ->
                    users.save(User.builder()
                            .name("Aarav Sharma").email("student@campusfix.com")
                            .password("student123").role("STUDENT").build()));

            User admin = users.findByEmail("admin@campusfix.com").orElseGet(() ->
                    users.save(User.builder()
                            .name("Campus Admin").email("admin@campusfix.com")
                            .password("admin123").role("ADMIN").build()));

            if (complaints.count() == 0) {
                add(complaints, student, "CF1024", "3 computers are not working in Lab 2.",
                        "Laboratory", "Block B", "Lab 2", "IT Support", "In Progress");
                add(complaints, student, "CF1025", "Ceiling fan is not working.",
                        "Electrical", "Block A", "Room 204", "Electrical Maintenance", "Pending");
                add(complaints, student, "CF1026", "Water leakage near the wash basin.",
                        "Plumbing", "Block C", "Washroom 1", "Civil & Maintenance", "Resolved");
                add(complaints, student, "CF1027", "Projector is showing a blank screen.",
                        "Projector / AV Equipment", "Block A", "Seminar Hall", "IT Support", "In Progress");
                add(complaints, student, "CF1028", "Campus Wi-Fi is unavailable.",
                        "Wi-Fi / Internet", "Library", "Ground Floor", "IT Support", "Resolved");
                add(complaints, student, "CF1029", "Broken bench in classroom.",
                        "Furniture", "Block B", "Room 105", "Facilities", "Pending");
                add(complaints, student, "CF1030", "Lights are flickering repeatedly.",
                        "Electrical", "Block C", "Room 301", "Electrical Maintenance", "In Progress");
                add(complaints, student, "CF1031", "Drinking water cooler is not working.",
                        "Water Supply", "Block A", "First Floor", "Civil & Maintenance", "Pending");
                add(complaints, student, "CF1032", "Laboratory sink tap is damaged.",
                        "Plumbing", "Science Block", "Chem Lab", "Civil & Maintenance", "Resolved");
                add(complaints, student, "CF1033", "Classroom projector remote is missing.",
                        "Projector / AV Equipment", "Block B", "Room 402", "IT Support", "Pending");
            }
        };
    }

    private void add(ComplaintRepository repo, User student, String id, String title, String category,
                     String building, String room, String dept, String status) {
        repo.save(Complaint.builder()
                .complaintId(id).title(title).description(title + " Please inspect and resolve this issue.")
                .category(category).building(building).roomNumber(room).department(dept).status(status)
                .student(student).submittedAt(LocalDateTime.now().minusDays((int)(Math.random()*10)))
                .updatedAt(LocalDateTime.now()).build());
    }
}
