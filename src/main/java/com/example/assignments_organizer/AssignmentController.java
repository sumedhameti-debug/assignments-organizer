package com.example.assignments_organizer;

import org.springframework.web.bind.annotation.*;
import java.util.List;

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
        return assignmentService.getAssignments();
    }

    @PostMapping("/assignments/run")
    public String runScheduler() {
        schedulerService.runScheduler();
        return "Scheduler executed.";
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
