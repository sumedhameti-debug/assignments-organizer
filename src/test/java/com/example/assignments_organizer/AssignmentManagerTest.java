package com.example.assignments_organizer;

import com.example.assignments_organizer.enums.Difficulty;
import com.example.assignments_organizer.model.Assignment;
import com.example.assignments_organizer.service.AssignmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AssignmentManagerTest {

    private AssignmentService assignmentService;
    private AssignmentManager assignmentManager;

    @BeforeEach
    void setup() {
        assignmentService = Mockito.mock(AssignmentService.class);
        assignmentManager = new AssignmentManager(assignmentService);
    }

    @Test
    void testCreateAssignment() {

        LocalDateTime due = LocalDateTime.now().plusDays(3);
        Assignment assignment = new Assignment(due, 120, Difficulty.MEDIUM, "Math HW");

        when(assignmentService.saveAssignment(any())).thenReturn(assignment);

        Assignment result = assignmentManager.createAssignment("Math HW", 120, Difficulty.MEDIUM, due);

        assertEquals("Math HW", result.getName());
        assertEquals(120, result.getDuration());

    }

    @Test
    void testCreateMultipleAssignments() {

        LocalDateTime due1 = LocalDateTime.now().plusDays(2);
        LocalDateTime due2 = LocalDateTime.now().plusDays(5);

        Assignment a1 = new Assignment(due1, 60, Difficulty.EASY, "HW1");
        Assignment a2 = new Assignment(due2, 90, Difficulty.HARD, "HW2");

        when(assignmentService.saveAssignment(any())).thenReturn(a1).thenReturn(a2);

        Assignment result1 = assignmentManager.createAssignment("HW1", 60, Difficulty.EASY, due1);
        Assignment result2 = assignmentManager.createAssignment("HW2", 90, Difficulty.HARD, due2);

        assertEquals("HW1", result1.getName());
        assertEquals("HW2", result2.getName());

    }

    @Test
    void testRetrieveAssignmentsWhenEmpty() {
        when(assignmentService.getAssignments()).thenReturn(new ArrayList<>());
        List<Assignment> assignments = assignmentManager.getAllAssignments();
        assertTrue(assignments.isEmpty());
    }

    @Test
    void testRetrieveAssignments() {

        List<Assignment> list = new ArrayList<>();
        list.add(new Assignment(LocalDateTime.now().plusDays(3), 120, Difficulty.MEDIUM, "Math HW"));

        when(assignmentService.getAssignments()).thenReturn(list);

        List<Assignment> result = assignmentManager.getAllAssignments();

        assertEquals(1, result.size());
        assertEquals("Math HW", result.get(0).getName());

    }

    @Test
    void testDeleteAssignment() {
        assignmentManager.deleteAssignment(1L);
        verify(assignmentService, times(1)).deleteAssignment(1L);
    }

    @Test
    void testUpdateAssignment() {

        LocalDateTime due = LocalDateTime.now().plusDays(4);

        Assignment updated = new Assignment(due, 100, Difficulty.HARD, "Updated HW");

        when(assignmentService.updateAssignment(anyLong(), any())).thenReturn(updated);

        Assignment result = assignmentManager.updateAssignment(1L, "Updated HW", 100, Difficulty.HARD, due);

        assertEquals("Updated HW", result.getName());
        assertEquals(100, result.getDuration());

    }

}