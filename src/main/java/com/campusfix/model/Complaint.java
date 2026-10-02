package com.campusfix.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Complaint {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String complaintId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String building;

    private String roomNumber;
    private String imagePath;
    private String department;
    private String status;
    private String resolutionNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    private User student;

    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;
}
