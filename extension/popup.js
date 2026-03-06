let assignments = [];
let currentIndex = 0;

let schedulerRunning = false;
let closeTimer = null;
const FIVE_MINUTES = 5 * 60 * 1000;

document.addEventListener('DOMContentLoaded', async () => {

    loadAssignments();

    document.getElementById('prevBtn').addEventListener('click', () => {
        clearStatus();
        prevAssignment();
    });
    document.getElementById('nextBtn').addEventListener('click', () => {
            clearStatus();
            nextAssignment();
    });
    document.getElementById('updateBtn').addEventListener('click', () => {
            clearStatus();
            saveAssignment();
    });
    document.getElementById('deleteBtn').addEventListener('click', () => {
            clearStatus();
            deleteAssignment()
    });

    document.getElementById('title').addEventListener('input', validateFields);
    document.getElementById('duration').addEventListener('input', validateFields);
    document.getElementById('difficulty').addEventListener('change', validateFields);
    document.getElementById('dueDate').addEventListener('input', validateFields);

    document.getElementById("studyDuration").addEventListener("input", validateSchedulerFields);
    document.getElementById("breakDuration").addEventListener("input", validateSchedulerFields);
    document.getElementById("inputCalendar").addEventListener("change", validateSchedulerFields);
    document.getElementById("outputCalendar").addEventListener("change", validateSchedulerFields);

    await loadCalendars();
    validateSchedulerFields();

    document.getElementById("runSchedulerBtn").addEventListener('click', () => {
            clearStatus();
            runScheduler();
    });

    startAutoCloseTimer();

});

function clearStatus() {
    const status = document.getElementById('status');
    status.textContent = "";
    status.style.color = "";
}

function startAutoCloseTimer() {

    if (schedulerRunning) return;

    if (closeTimer) {
        clearTimeout(closeTimer);
    }

    closeTimer = setTimeout(() => {
        window.close();
    }, FIVE_MINUTES);
}

function cancelAutoCloseTimer() {
    if (closeTimer) {
        clearTimeout(closeTimer);
        closeTimer = null;
    }
}

function loadAssignments() {
    fetch('http://localhost:8080/assignments')
        .then(res => res.json())
        .then(data => {
            assignments = data;
            currentIndex = 0;
            renderAssignment();
        });
}

function renderAssignment() {

    const counter = document.getElementById('counter');
    const deleteBtn = document.getElementById('deleteBtn');
    const runSchedulerBtn = document.getElementById('runSchedulerBtn');

    if (assignments.length === 0) {

        document.getElementById('title').value = "";
        document.getElementById('duration').value = "";
        document.getElementById('difficulty').value = "";

        const dueDateInput = document.getElementById('dueDate');
        const t = new Date();
        const today = t.getFullYear() + '-' +
            String(t.getMonth() + 1).padStart(2, '0') + '-' +
            String(t.getDate()).padStart(2, '0');
        dueDateInput.min = today;
        dueDateInput.value = today;

        counter.textContent = "No assignments";

        deleteBtn.disabled = true;
        runSchedulerBtn.disabled = true;

        validateFields();
        return;
    }

    runSchedulerBtn.disabled = false;

    if (currentIndex < assignments.length) {

        const a = assignments[currentIndex];

        document.getElementById('title').value = a.name;
        document.getElementById('duration').value = a.duration;
        document.getElementById('difficulty').value = a.difficulty;
        document.getElementById('dueDate').value = a.lastDateLocalDateTime.split("T")[0];

        console.log(a);

        counter.textContent = `${currentIndex + 1} / ${assignments.length}`;

        deleteBtn.disabled = false;

    } else {

        document.getElementById('title').value = "";
        document.getElementById('duration').value = "";
        document.getElementById('difficulty').value = "";

        const dueDateInput = document.getElementById('dueDate');

        const t = new Date();
        const today = t.getFullYear() + '-' +
            String(t.getMonth() + 1).padStart(2, '0') + '-' +
            String(t.getDate()).padStart(2, '0');
        dueDateInput.min = today;
        dueDateInput.value = today;

        counter.textContent = `New`;

        deleteBtn.disabled = true;

    }

    validateFields();

}

function nextAssignment() {
    if (currentIndex < assignments.length) {
        currentIndex++;
        renderAssignment();
    }
}

function prevAssignment() {
    if (currentIndex > 0) {
        currentIndex--;
        renderAssignment();
    }
}

function saveAssignment() {

    const selectedDate = document.getElementById('dueDate').value;

    const assignmentData = {
        name: document.getElementById('title').value,
        duration: parseInt(document.getElementById('duration').value),
        difficulty: document.getElementById('difficulty').value,
        lastDateLocalDateTime: selectedDate ? selectedDate + "T23:59:00" : null
    };

    if (currentIndex < assignments.length) {
        fetch(`http://localhost:8080/assignments/${assignments[currentIndex].id}`, {
            method: 'PUT',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(assignmentData)
        })
            .then(res => res.json())
            .then(updatedAssignment => {
        assignments[currentIndex] = updatedAssignment;
        renderAssignment();
        document.getElementById('status').textContent = "Assignment updated!";
            })
            .catch(err => {
        console.error(err);
        document.getElementById('status').textContent = "Error updating assignment.";
            });

    } else {
        fetch('http://localhost:8080/assignments', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(assignmentData)
        })
            .then(res => res.json())
            .then(newAssignment => {
        assignments.push(newAssignment);
        currentIndex = assignments.length - 1;
        renderAssignment();
        document.getElementById('status').textContent = "Assignment created!";
            })
            .catch(err => {
        console.error(err);
        document.getElementById('status').textContent = "Error creating assignment.";
            });
    }

    console.log("Saving assignment", assignmentData);

}

function deleteAssignment() {

    if (currentIndex >= assignments.length) return;

    fetch(`http://localhost:8080/assignments/${assignments[currentIndex].id}`, {
        method: 'DELETE'
    }).then(() => loadAssignments());
}

function validateSchedulerFields() {

    const study = document.getElementById("studyDuration").value.trim();
    const brk = document.getElementById("breakDuration").value.trim();
    const inputCal = document.getElementById("inputCalendar").value;
    const outputCal = document.getElementById("outputCalendar").value;

    const runBtn = document.getElementById("runSchedulerBtn");

    const studyValid = study && parseInt(study) > 0;
    const breakValid = brk && parseInt(brk) >= 0;
    const calendarsValid = inputCal && outputCal;

    runBtn.disabled = !(studyValid && breakValid && calendarsValid);
}

function validateFields() {

    const difficultySelect = document.getElementById('difficulty');

    if (difficultySelect.value === "") {
        difficultySelect.classList.add("placeholder");
    } else {
        difficultySelect.classList.remove("placeholder");
    }

    const title = document.getElementById('title').value.trim();
    const duration = document.getElementById('duration').value.trim();
    const difficulty = document.getElementById('difficulty').value;
    const dueDate = document.getElementById('dueDate').value.trim();

    const updateBtn = document.getElementById('updateBtn');

    const allFilled = title && duration && difficulty && dueDate;

    const isNew = currentIndex === assignments.length;

    let isChanged = false;

    if (!isNew && currentIndex < assignments.length) {
        const currentAssignment = assignments[currentIndex];

        isChanged =
            title !== currentAssignment.name ||
            parseInt(duration) !== currentAssignment.duration ||
            difficulty !== currentAssignment.difficulty ||
            dueDate !== currentAssignment.lastDateLocalDateTime.split("T")[0];
    }

    updateBtn.disabled = !(allFilled && (isNew || isChanged));

}

async function getAuthToken() {
    return new Promise((resolve, reject) => {
        chrome.identity.getAuthToken({ interactive: true }, function(token) {
            if (chrome.runtime.lastError || !token) {
                reject(chrome.runtime.lastError);
            } else {
                resolve(token);
            }
        });
    });
}

async function loadCalendars() {

    try {

        const token = await getAuthToken();
        console.log("Token: ", token);

        const response = await fetch("https://www.googleapis.com/calendar/v3/users/me/calendarList", {
            headers: { "Authorization": "Bearer " + token }
        });

        const data = await response.json();
        const calendars = data.items || [];

        const inputSelect = document.getElementById("inputCalendar");
        const outputSelect = document.getElementById("outputCalendar");

        inputSelect.innerHTML = '<option value="" disabled selected hidden>Select input calendar</option>';
        outputSelect.innerHTML = '<option value="" disabled selected hidden>Select output calendar</option>';

        calendars.forEach(cal => {

            const option1 = document.createElement("option");
            option1.value = cal.id;
            option1.textContent = cal.summary;
            inputSelect.appendChild(option1);

            const option2 = document.createElement("option");
            option2.value = cal.id;
            option2.textContent = cal.summary;
            outputSelect.appendChild(option2);

        });

    } catch (err) {

        console.error("Error loading calendars:", err);
        const status = document.getElementById('status');
        status.style.color = "red";
        status.textContent = "Error loading calendars. Make sure you are signed in.";

    }

}

async function runScheduler() {

    const selectedInput = document.getElementById("inputCalendar").value;
    const selectedOutput = document.getElementById("outputCalendar").value;
    const studyDuration = parseInt(document.getElementById("studyDuration").value);
    const breakDuration = parseInt(document.getElementById("breakDuration").value);

    if (!studyDuration || studyDuration <= 0) {
        status.textContent = "Study duration must be greater than 0.";
        return;
    } else if (breakDuration < 0) {
        status.textContent = "Break duration cannot be negative.";
        return;
    } else if (!selectedInput || !selectedOutput) {
        const status = document.getElementById('status');
        status.style.color = "red";
        status.textContent = "Please select both input and output calendars.";
        return;
    }

    try {

        schedulerRunning = true;
        cancelAutoCloseTimer();

        const token = await getAuthToken();

        const response = await fetch("http://localhost:8080/assignments/run", {
            method: "POST",
            headers: {
                "Authorization": "Bearer " + token,
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                inputCalendarId: selectedInput,
                outputCalendarId: selectedOutput,
                studyDuration: studyDuration,
                breakDuration: breakDuration
            })
        });

        if (!response.ok) {
            const errorText = await response.text();

            if (errorText.includes("Not enough time to study")) {
                throw new Error(
                    "There is not enough time to study in Calendar, please adjust the available time."
                );
            }

            throw new Error(errorText);
        }

        const data = await response.text();

        const status = document.getElementById('status');
        status.style.color = "green";
        status.textContent = data;

        loadAssignments();

    } catch (err) {
        const status = document.getElementById('status');
        status.style.color = "red";
        status.textContent = err.message;
    } finally {
        schedulerRunning = false;
        startAutoCloseTimer();
    }
}

function getDueDateFromDaysAhead(daysAhead) {

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const dueDate = new Date(today);
    dueDate.setDate(today.getDate() + daysAhead);

    const year = dueDate.getFullYear();
    const month = String(dueDate.getMonth() + 1).padStart(2, "0");
    const day = String(dueDate.getDate()).padStart(2, "0");

    return `${year}-${month}-${day}`;

}