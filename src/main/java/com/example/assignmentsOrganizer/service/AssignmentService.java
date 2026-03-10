package com.example.assignmentsOrganizer.service;

import com.example.assignmentsOrganizer.model.Assignment;
import com.example.assignmentsOrganizer.repository.AssignmentRepository;
import com.example.assignmentsOrganizer.enums.Difficulty;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Handles database interactions
 */
@Service
public class AssignmentService {

    private final AssignmentRepository repository;

    public AssignmentService(AssignmentRepository repository) {
        this.repository = repository;
    }

    public Assignment saveAssignment(Assignment assignment) {
        return repository.save(assignment);
    }

    public List<Assignment> getAssignments() {
        return repository.findAll();
    }

    public Assignment updateAssignment(Long id, Assignment updatedAssignment) {

        Assignment existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        existing.setName(updatedAssignment.getName());
        existing.setDifficulty(Difficulty.valueOf(updatedAssignment.getDifficulty().toString().toUpperCase()));
        existing.setDuration(updatedAssignment.getDuration());
        existing.setLastDate(updatedAssignment.getLastDate());
        existing.setLastDateLocalDateTime(updatedAssignment.getLastDateLocalDateTime());

        return repository.save(existing);

    }

    public void deleteAssignment(Long id) {
        repository.deleteById(id);
    }

}
