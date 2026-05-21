/**
 * Tasks page - CRUD operations, search and filters.
 */

let taskModal;
let editingTaskId = null;
let tasksCache = {}; // Store tasks by id for edit button

document.addEventListener('DOMContentLoaded', async () => {
    const user = await requireAuth();
    if (!user) return;

    setupSidebar();
    taskModal = new bootstrap.Modal(document.getElementById('taskModal'));

    document.getElementById('logoutBtn').addEventListener('click', logoutUser);
    document.getElementById('applyFilterBtn').addEventListener('click', loadTasks);
    document.getElementById('saveTaskBtn').addEventListener('click', saveTask);

    // Search on Enter key
    document.getElementById('searchInput').addEventListener('keyup', (e) => {
        if (e.key === 'Enter') loadTasks();
    });

    loadTasks();
});

/**
 * Load tasks with current filter values.
 */
async function loadTasks() {
    const search = document.getElementById('searchInput').value.trim();
    const priority = document.getElementById('filterPriority').value;
    const status = document.getElementById('filterStatus').value;
    const dueDate = document.getElementById('filterDueDate').value;

    let query = `/api/tasks?priority=${priority}&status=${status}&dueDate=${dueDate}`;
    if (search) query += `&search=${encodeURIComponent(search)}`;

    try {
        const { response, data } = await apiRequest(query);

        if (response.ok && data.success) {
            renderTasks(data.tasks);
        }
    } catch (err) {
        showAlert('alertBox', 'Failed to load tasks');
    }
}

/**
 * Display tasks as cards with action buttons.
 */
function renderTasks(tasks) {
    const container = document.getElementById('tasksList');

    if (!tasks || tasks.length === 0) {
        container.innerHTML = '<p class="text-muted">No tasks found. Click "Add Task" to create one.</p>';
        return;
    }

    let html = '';
    tasksCache = {};

    tasks.forEach(task => {
        tasksCache[task.id] = task;
        const priorityClass = task.priority.toLowerCase();
        const isCompleted = task.status === 'completed';

        html += `
            <div class="task-card ${isCompleted ? 'completed' : ''}">
                <div class="d-flex justify-content-between flex-wrap gap-2">
                    <div style="flex:1;">
                        <div class="task-title">${escapeHtml(task.title)}</div>
                        <div class="task-desc">${escapeHtml(task.description || '')}</div>
                        <div class="mt-2">
                            <span class="badge-priority ${priorityClass}">${task.priority}</span>
                            <span class="badge-status ${task.status}">${task.status}</span>
                            <small class="text-muted ms-2">Due: ${task.dueDate || 'N/A'}</small>
                        </div>
                    </div>
                    <div class="d-flex gap-2 align-items-start flex-wrap">
                        ${!isCompleted ? `<button class="btn btn-sm btn-success" onclick="completeTask(${task.id})"><i class="bi bi-check-lg"></i></button>` : ''}
                        <button class="btn btn-sm btn-action" onclick="openEditModal(${task.id})"><i class="bi bi-pencil"></i></button>
                        <button class="btn btn-sm btn-outline-danger" onclick="deleteTask(${task.id})"><i class="bi bi-trash"></i></button>
                    </div>
                </div>
            </div>
        `;
    });

    container.innerHTML = html;
}

/**
 * Open modal for adding new task.
 */
function openAddModal() {
    editingTaskId = null;
    document.getElementById('modalTitle').textContent = 'Add Task';
    document.getElementById('taskForm').reset();
    document.getElementById('taskId').value = '';
    document.getElementById('statusField').style.display = 'none';
}

/**
 * Open modal for editing existing task.
 */
function openEditModal(taskId) {
    const task = tasksCache[taskId];
    if (!task) return;

    editingTaskId = task.id;
    document.getElementById('modalTitle').textContent = 'Edit Task';
    document.getElementById('taskId').value = task.id;
    document.getElementById('taskTitle').value = task.title;
    document.getElementById('taskDescription').value = task.description || '';
    document.getElementById('taskPriority').value = task.priority;
    document.getElementById('taskDueDate').value = task.dueDate || '';
    document.getElementById('taskStatus').value = task.status;
    document.getElementById('statusField').style.display = 'block';
    taskModal.show();
}

/**
 * Save task - calls POST for new or PUT for edit.
 */
async function saveTask() {
    const title = document.getElementById('taskTitle').value.trim();
    if (!title) {
        alert('Please enter task title');
        return;
    }

    const taskData = {
        title: title,
        description: document.getElementById('taskDescription').value,
        priority: document.getElementById('taskPriority').value,
        dueDate: document.getElementById('taskDueDate').value,
        status: document.getElementById('taskStatus').value || 'pending'
    };

    try {
        let url = '/api/tasks';
        let method = 'POST';

        if (editingTaskId) {
            url = `/api/tasks/${editingTaskId}`;
            method = 'PUT';
        }

        const { response, data } = await apiRequest(url, {
            method: method,
            body: JSON.stringify(taskData)
        });

        if (response.ok && data.success) {
            taskModal.hide();
            showAlert('alertBox', data.message, 'success');
            loadTasks();
        } else {
            showAlert('alertBox', data.message || 'Failed to save task');
        }
    } catch (err) {
        showAlert('alertBox', 'Server error while saving task');
    }
}

/**
 * Mark task as completed.
 */
async function completeTask(taskId) {
    const { response, data } = await apiRequest(`/api/tasks/${taskId}/complete`, { method: 'PUT' });

    if (response.ok && data.success) {
        showAlert('alertBox', 'Task completed!', 'success');
        loadTasks();
    } else {
        showAlert('alertBox', data.message || 'Failed to complete task');
    }
}

/**
 * Delete task after confirmation.
 */
async function deleteTask(taskId) {
    if (!confirm('Are you sure you want to delete this task?')) return;

    const { response, data } = await apiRequest(`/api/tasks/${taskId}`, { method: 'DELETE' });

    if (response.ok && data.success) {
        showAlert('alertBox', 'Task deleted', 'success');
        loadTasks();
    } else {
        showAlert('alertBox', data.message || 'Failed to delete task');
    }
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
