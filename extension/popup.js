let assignments = [];
let currentIndex = 0;

document.addEventListener('DOMContentLoaded', () => {

    loadAssignments();

    document.getElementById('prevBtn').addEventListener('click', prevAssignment);
    document.getElementById('nextBtn').addEventListener('click', nextAssignment);
    document.getElementById('updateBtn').addEventListener('click', saveAssignment);
    document.getElementById('deleteBtn').addEventListener('click', deleteAssignment);
    document.getElementById('runSchedulerBtn').addEventListener('click', runScheduler);

    document.getElementById('title').addEventListener('input', validateFields);
    document.getElementById('duration').addEventListener('input', validateFields);
    document.getElementById('difficulty').addEventListener('change', validateFields);
    document.getElementById('dueDate').addEventListener('input', validateFields);

});

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
        document.getElementById('dueDate').value = "";

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
        document.getElementById('dueDate').value = a.lastDate;

        counter.textContent = `${currentIndex + 1} / ${assignments.length}`;

	deleteBtn.disabled = false;

    } else {

        document.getElementById('title').value = "";
        document.getElementById('duration').value = "";
        document.getElementById('difficulty').value = "";
        document.getElementById('dueDate').value = "";

        counter.textContent = `New`;

	deleteBtn.disabled = true;
	
    }

    validateFields();
    
}

function nextAssignment() {
    if (currentIndex <= assignments.length) {
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

    const assignmentData = {
        name: document.getElementById('title').value,
        duration: parseInt(document.getElementById('duration').value),
        difficulty: document.getElementById('difficulty').value,
        lastDate: parseInt(document.getElementById('dueDate').value)
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

    renderAssignment();
    
}

function deleteAssignment() {

    if (currentIndex >= assignments.length) return;

    fetch(`http://localhost:8080/assignments/${assignments[currentIndex].id}`, {
        method: 'DELETE'
    }).then(() => loadAssignments());
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
            parseInt(dueDate) !== currentAssignment.lastDate;
    }

    updateBtn.disabled = !(allFilled && (isNew || isChanged));
    
}


function runScheduler() {
    fetch('http://localhost:8080/assignments/run', {
        method: 'POST'
    })
	.then(response => response.text())
	.then(data => {
            document.getElementById('status').textContent = data;
            loadAssignments();
	})
	.catch(err => {
            console.error("Error running scheduler:", err);
            document.getElementById('status').textContent = "Error running scheduler.";
	});
}
