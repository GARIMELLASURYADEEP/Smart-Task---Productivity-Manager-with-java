/**
 * Analytics page - Chart.js charts for productivity insights.
 */

let priorityChart, statusChart, weeklyChart;
let analyticsCache = null; // Store data for theme refresh

document.addEventListener('DOMContentLoaded', async () => {
    const user = await requireAuth();
    if (!user) return;

    setupSidebar();
    document.getElementById('logoutBtn').addEventListener('click', logoutUser);

    loadAnalytics();

    // Re-draw charts when day/night theme changes
    window.addEventListener('themeChanged', () => {
        if (analyticsCache) {
            renderPriorityChart(analyticsCache.priority);
            renderStatusChart(analyticsCache.status);
            renderWeeklyChart(analyticsCache.weekly);
        }
    });
});

/** Chart text/grid colors based on current theme */
function getChartColors() {
    const isLight = document.documentElement.getAttribute('data-theme') === 'light';
    return {
        text: isLight ? '#64748b' : '#94a3b8',
        grid: isLight ? '#e2e8f0' : '#2d3a4f'
    };
}

/**
 * Fetch analytics data and render Chart.js charts.
 */
async function loadAnalytics() {
    try {
        const { response, data } = await apiRequest('/api/analytics');

        if (!response.ok || !data.success) return;

        analyticsCache = data;
        renderPriorityChart(data.priority);
        renderStatusChart(data.status);
        renderWeeklyChart(data.weekly);

    } catch (err) {
        console.error('Analytics error:', err);
    }
}

/**
 * Pie chart - tasks by priority (High, Medium, Low).
 */
function renderPriorityChart(priorityData) {
    const ctx = document.getElementById('priorityChart').getContext('2d');

    const labels = ['High', 'Medium', 'Low'];
    const values = labels.map(label => priorityData[label] || 0);

    if (priorityChart) priorityChart.destroy();

    const colors = getChartColors();

    priorityChart = new Chart(ctx, {
        type: 'pie',
        data: {
            labels: labels,
            datasets: [{
                data: values,
                backgroundColor: ['#ef4444', '#f59e0b', '#22c55e'],
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    labels: { color: colors.text }
                }
            }
        }
    });
}

/**
 * Doughnut chart - pending vs completed tasks.
 */
function renderStatusChart(statusData) {
    const ctx = document.getElementById('statusChart').getContext('2d');

    const labels = ['pending', 'completed'];
    const values = labels.map(label => statusData[label] || 0);

    if (statusChart) statusChart.destroy();

    const colors = getChartColors();

    statusChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['Pending', 'Completed'],
            datasets: [{
                data: values,
                backgroundColor: ['#f59e0b', '#22c55e'],
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    labels: { color: colors.text }
                }
            }
        }
    });
}

/**
 * Bar chart - weekly productivity (completed tasks per day).
 */
function renderWeeklyChart(weeklyData) {
    const ctx = document.getElementById('weeklyChart').getContext('2d');

    const labels = weeklyData.map(item => item.day);
    const completed = weeklyData.map(item => item.completed);
    const total = weeklyData.map(item => item.total);

    if (weeklyChart) weeklyChart.destroy();

    const colors = getChartColors();

    weeklyChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels.length ? labels : ['No data'],
            datasets: [
                {
                    label: 'Completed',
                    data: completed.length ? completed : [0],
                    backgroundColor: '#22c55e'
                },
                {
                    label: 'Total',
                    data: total.length ? total : [0],
                    backgroundColor: '#6366f1'
                }
            ]
        },
        options: {
            responsive: true,
            scales: {
                x: {
                    ticks: { color: colors.text },
                    grid: { color: colors.grid }
                },
                y: {
                    ticks: { color: colors.text },
                    grid: { color: colors.grid },
                    beginAtZero: true
                }
            },
            plugins: {
                legend: {
                    labels: { color: colors.text }
                }
            }
        }
    });
}
