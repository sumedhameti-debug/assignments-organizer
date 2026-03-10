# Assignments Organizer

This full-stack Chrome Extension automatically schedules assignments into Google Calendar based on deadline, difficulty, and estimated duration. It eliminates manual planning and optimizes study time allocation using a greedy scheduling algorithm.

## Overview

Assignments Organizer allows students to:

- Input assignments with deadline, duration, and difficulty.
- Generate study sessions by spreading out challenging assignments to minimize burnout, as well as scheduling breaks.
- View scheduled sessions directly in Google Calendar.

## System Architecture

### Frontend
- **Chrome Extension (Popup UI)**
    - Allows users to create and manage assignments
    - Retrieves available calendars
    - Sends scheduling requests to the backend

### Backend
- **Spring Boot REST API**
    - Exposes endpoints for assignment management and scheduling
    - Runs the scheduling algorithm
    - Communicates with external services
- **Scheduling Engine**
    - Implements a greedy scheduling algorithm
    - Distributes assignment workload across available days
    - Ensures deadlines are met while balancing daily workload

### Storage
- **MySQL Database**
    - Stores assignments and related data

### External Services
- **Google Calendar API**
    - Creates study session events in the user's calendar

## Tech Stack

- **Frontend:** Chrome Extension (Manifest v3, JavaScript).
- **Backend:** Java + Spring Boot.
- **Database:** MySQL.
- **External Integration:** Google Calendar API (OAuth2).

## Scheduling Algorithm

The scheduler models assignment planning as a constrained load-balancing problem.

### Objectives
- Ensure completion before deadlines.
- Balance workload across difficulty levels.
- Avoid overloading individual days.

### Algorithm
1) Groups assignments by difficulty.
2) Sorts them by deadline.
3) Distributes ideal workload across days.
4) Redistributes excess load backward from deadlines.
5) Rebalances difficulty allocation to smooth daily workload.

This approach uses a greedy heuristic with local redistribution.
Worst-case runtime is cubic in the number of assignments due to repeated conflict checks and redistribution loops. 

## Running the Project

### Clone the Repository
```bash
git clone https://github.com/<your-username>/assignments-organizer.git
cd assignments-organizer
```

### Start the Backend Server

Navigate to the backend directory and start the Spring Boot application.

```bash
cd backend
./mvnw spring-boot:run
```

The backend will run at:

```bash
http://localhost:8080
```

### Load the Chrome Extension

1. Open Chrome and go to 

```bash
chrome://extensions
```

2. Enable Developer Mode (top right)

3. Click Load Unpacked

4. Select the extension/ folder from the repository


### Use the Extension

1. Navigate to Google Calendar 
2. Make events in a calendar (input calendar) to represent time available for studying 
3. Create a new calendar (output calendar) where events can be scheduled 
4. Click the extension icon in Chrome Authenticate with your Google account when prompted 
5. Add assignments in the popup interface (scroll to the end to add new assignments)
6. Select input and output calendars along with the necessary study and break duration
7. Click Run Scheduler to generate study sessions in the output calendar