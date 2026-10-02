package com.campusfix.repository;

import com.campusfix.model.Complaint;
import com.campusfix.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByStudentOrderBySubmittedAtDesc(User student);
    Optional<Complaint> findByComplaintId(String complaintId);
    long countByStatus(String status);
}
