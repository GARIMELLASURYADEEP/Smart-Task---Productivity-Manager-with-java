/**
 * Dashboard page - loads stats and recent tasks.
 */

let currentUser = null;

document.addEventListener('DOMContentLoaded', async () => {
    currentUser = await requireAuth();
    if (!currentUser) return;

    document.getElementById('usernameDisplay').textContent = currentUser.username;

    setupSidebar();
    document.getElementById('logoutBtn').addEventListener('click', logoutUser);

    loadDashboard();
});

/**
 * Fetch dashboard data from Java API and update UI.
 */
async function loadDashboard() {
    try {
        const { response, data } = await apiRequest('/api/dashboard');

        if (!response.ok || !data.success) {
            return;
        }

        const stats = data.stats;

        // Update stat cards
        document.getElementById('totalTasks').textContent = stats.total;
        document.getElementById('completedTasks').textContent = stats.completed;
        document.getElementById('pendingTasks').textContent = stats.pending;
        document.getElementById('overdueTasks').textContent = stats.overdue;

        // Update productivity bar
        const productivity = stats.productivity || 0;
        document.getElementById('productivityText').textContent = productivity + '%';
        document.getElementById('productivityBar').style.width = productivity + '%';

        const label = document.getElementById('productivityLabel');
        if (productivity >= 80) label.textContent = 'Excellent work!';
        else if (productivity >= 50) label.textContent = 'Good progress!';
        else label.textContent = 'Keep going!';

        // Show recent tasks
        renderRecentTasks(data.recentTasks);

    } catch (err) {
        console.error('Dashboard load error:', err);
    }
}

/**
 * Render recent task list on dashboard.
 */
function renderRecentTasks(tasks) {
    const container = document.getElementById('recentTasksList');

    if (!tasks || tasks.length === 0) {
        container.innerHTML = '<p class="text-muted">No tasks yet. Go to Tasks page to add one!</p>';
        return;
    }

    let html = '';
    tasks.forEach(task => {
        const priorityClass = task.priority.toLowerCase();
        const isCompleted = task.status === 'completed';

        html += `
            <div class="task-card ${isCompleted ? 'completed' : ''}">
                <div class="d-flex justify-content-between align-items-start">
                    <div>
                        <div class="task-title">${escapeHtml(task.title)}</div>
                        <div class="task-desc">${escapeHtml(task.description || 'No description')}</div>
                        <small class="text-muted">Due: ${task.dueDate || 'Not set'}</small>
                    </div>
                    <div class="d-flex gap-2">
                        <span class="badge-priority ${priorityClass}">${task.priority}</span>
                        <span class="badge-status ${task.status}">${task.status}</span>
                    </div>
                </div>
            </div>
        `;
    });

    container.innerHTML = html;
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
