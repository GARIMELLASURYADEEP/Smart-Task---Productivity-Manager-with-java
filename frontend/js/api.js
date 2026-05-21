/**
 * Common API helper - used by all frontend pages.
 * Sends requests to Java backend with session cookie.
 */

const API_BASE = ''; // Same server, so relative URLs work

/**
 * Generic fetch wrapper with credentials (cookies).
 */
async function apiRequest(url, options = {}) {
    const defaultOptions = {
        credentials: 'include', // Important: sends session cookie
        headers: {
            'Content-Type': 'application/json'
        }
    };

    const response = await fetch(API_BASE + url, { ...defaultOptions, ...options });
    const data = await response.json();
    return { response, data };
}

/**
 * Check if user is logged in. Redirect to login if not.
 */
async function requireAuth() {
    const { response, data } = await apiRequest('/api/auth/me');

    if (!response.ok || !data.success) {
        window.location.href = 'login.html';
        return null;
    }

    return data.user;
}

/**
 * Logout user and redirect to login page.
 */
async function logoutUser() {
    await apiRequest('/api/auth/logout', { method: 'POST' });
    window.location.href = 'login.html';
}

/**
 * Show alert message on page.
 */
function showAlert(elementId, message, type = 'error') {
    const alertBox = document.getElementById(elementId);
    if (!alertBox) return;

    alertBox.textContent = message;
    alertBox.className = 'alert-custom show alert-' + type;
}

/**
 * Hide alert box.
 */
function hideAlert(elementId) {
    const alertBox = document.getElementById(elementId);
    if (alertBox) {
        alertBox.className = 'alert-custom';
    }
}

/**
 * Toggle mobile sidebar.
 */
function setupSidebar() {
    const toggleBtn = document.getElementById('menuToggle');
    const sidebar = document.getElementById('sidebar');

    if (toggleBtn && sidebar) {
        toggleBtn.addEventListener('click', () => {
            sidebar.classList.toggle('open');
        });
    }

    // Highlight active nav link based on current page
    const currentPage = window.location.pathname.split('/').pop();
    document.querySelectorAll('.sidebar .nav-link').forEach(link => {
        if (link.getAttribute('href') === currentPage) {
            link.classList.add('active');
        }
    });
}
