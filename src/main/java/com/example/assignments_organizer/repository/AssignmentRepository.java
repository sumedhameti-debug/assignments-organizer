package com.example.assignments_organizer.repository;

import com.example.assignments_organizer.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Data access layer for Assignment entities using Spring Data JPA.
 */
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findAll();

}