package com.example.assignments_organizer;

import com.example.assignments_organizer.enums.Difficulty;
import com.example.assignments_organizer.model.Assignment;
import com.example.assignments_organizer.service.AssignmentService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * High level class used to perform common assignment operations
 * using the AssignmentService layer.
 */
@Component
public class AssignmentManager {

    private final AssignmentService assignmentService;

    public AssignmentManager(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    /**
     * Creates and saves a new assignment
     */
    public Assignment createAssignment(String name, int duration, Difficulty difficulty, LocalDateTime dueDate) {
        Assignment assignment = new Assignment(dueDate, duration, difficulty, name);
        assignment.setLastDateLocalDateTime(dueDate);
        return assignmentService.saveAssignment(assignment);
    }

    /**
     * Returns all assignments stored in the system
     */
    public List<Assignment> getAllAssignments() {
        return assignmentService.getAssignments();
    }

    /**
     * Updates an assignment by id
     */
    public Assignment updateAssignment(Long id, String name, int duration, Difficulty difficulty, LocalDateTime dueDate) {
        Assignment updated = new Assignment(dueDate, duration, difficulty, name);
        updated.setLastDateLocalDateTime(dueDate);
        return assignmentService.updateAssignment(id, updated);
    }

    /**
     * Deletes an assignment by id
     */
    public void deleteAssignment(Long id) {
        assignmentService.deleteAssignment(id);
    }

}