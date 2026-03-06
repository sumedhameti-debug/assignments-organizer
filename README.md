# Assignments Organizer

This full-stack Chrome Extension automatically schedules assignments into Google Calendar based on deadline, difficulty, and estimated duration. It eliminates manual planning and optimizes study time allocation using a greedy scheduling algorithm.

## Overview

Assignments Organizer allows students to:

- Input assignments with deadline, duration, and difficulty.
- Generate study sessions by spreading out challenging assignments to minimize burnout, as well as scheduling breaks.
- View scheduled sessions directly in Google Calendar.

## System Architecture

Chrome Extension (Popup UI)
→
Spring Boot REST API
→
Scheduling Engine (Greedy Algorithm)
→
MySQL Database
→
Google Calendar API

- Frontend: Chrome Extension (Manifest v3, JavaScript).
- Backend: Java + Spring Boot.
- Database: MySQL.
- External Integration: Google Calendar API (OAuth2).

## Scheduling Algorithm


The scheduler models assignment planning as a constrained load-balancing problem.

Objectives:
- Ensure completion before deadlines.
- Balance workload across difficulty levels.
- Avoid overloading individual days.

The algorithm:
1) Groups assignments by difficulty.
2) Sorts them by deadline.
3) Distributes ideal workload across days.
4) Redistributes excess load backward from deadlines.
5) Rebalances difficulty allocation to smooth daily workload.
6) This approach uses a greedy heuristic with local redistribution.

Worst-case runtime is cubic in the number of assignments due to repeated conflict checks and redistribution loops. <!-- However, performance remains sub-second under realistic student workloads. -->

<!--
## Testing

Unit tests cover:
- Scheduling logic correctness
- Conflict detection
- Edge cases (same deadline, insufficient time, empty input)
- Priority ordering

Run tests:
mvn test

## Setup Instructions
1️⃣ Backend Setup
Requirements

Java 17+

MySQL

Maven

Configure Database

Create database:

CREATE DATABASE assignments_db;

Update application.properties:

spring.datasource.url=jdbc:mysql://localhost:3306/assignments_db
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD

Run backend:

mvn spring-boot:run

Backend runs on:

http://localhost:8080
2️⃣ Google Calendar API Setup

Create project in Google Cloud Console

Enable Google Calendar API

Configure OAuth credentials

Add redirect URI

Insert client ID into backend config

3️⃣ Load Chrome Extension

Open Chrome

Navigate to:

chrome://extensions

Enable Developer Mode

Click Load unpacked

Select the extension directory

# Performance

Tested with 100+ assignments:

Scheduling completes under 200ms

No observable degradation at typical student workloads

-->