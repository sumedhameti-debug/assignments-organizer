package com.example.assignments_organizer.controller;

import com.example.assignments_organizer.model.Assignment;
import com.example.assignments_organizer.service.AssignmentService;
import com.example.assignments_organizer.model.ScheduleRequest;
import com.example.assignments_organizer.service.SchedulerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Handles interactions with the popup
 */
@RestController
public class AssignmentController {

    private final SchedulerService schedulerService;
    private final AssignmentService assignmentService;

    public AssignmentController(SchedulerService schedulerService, AssignmentService assignmentService) {
        this.schedulerService = schedulerService;
        this.assignmentService = assignmentService;
    }

    @PostMapping("/assignments")
    public Assignment addAssignment(@RequestBody Assignment assignment) {
        return assignmentService.saveAssignment(assignment);
    }

    @GetMapping("/assignments")
    public List<Assignment> getAssignments() {
        List<Assignment> assignments = assignmentService.getAssignments();
        LocalDateTime today = LocalDateTime.now();
        for (Assignment assignment : assignments) {
            assignment.setLastDateToDaysFromToday(today);
            if (assignment.getLastDate() < 1) assignmentService.deleteAssignment(assignment.getId());
        }
        return assignments;
    }

    @PostMapping("/assignments/run")
    public ResponseEntity<String> runScheduler(@RequestHeader("Authorization") String authHeader, @RequestBody ScheduleRequest request) {

        try {
            String token = authHeader.replace("Bearer ", "");
            schedulerService.runScheduler(token, request);
            return ResponseEntity.ok("Scheduler executed.");
        } catch (ResponseStatusException e) {

            e.printStackTrace();

            if (e.getReason() != null && e.getReason().contains("Not enough time to study")) {
                return ResponseEntity
                        .badRequest()
                        .body("Not enough time to study");
            }

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected server error");

        } catch (RuntimeException e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected server error");

        }

    }

    @PutMapping("/assignments/{id}")
    public Assignment updateAssignment(@PathVariable Long id, @RequestBody Assignment updatedAssignment) {
        return assignmentService.updateAssignment(id, updatedAssignment);
    }

    @DeleteMapping("/assignments/{id}")
    public void deleteAssignment(@PathVariable Long id) {
        assignmentService.deleteAssignment(id);
    }

}
