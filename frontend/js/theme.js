/**
 * Simple Day / Night theme toggle
 * - Saves choice in localStorage
 * - Uses data-theme attribute on <html>
 */

const THEME_KEY = 'smarttask-theme';

/**
 * Apply theme to the page (dark = night, light = day)
 */
function applyTheme(theme) {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem(THEME_KEY, theme);
    updateToggleButtons(theme);
}

/**
 * Switch between light and dark
 */
function toggleTheme() {
    const current = document.documentElement.getAttribute('data-theme') || 'dark';
    const next = current === 'dark' ? 'light' : 'dark';
    applyTheme(next);

    // Let other scripts (e.g. charts) react to theme change
    window.dispatchEvent(new CustomEvent('themeChanged', { detail: { theme: next } }));
}

/**
 * Update all theme toggle buttons on the page
 */
function updateToggleButtons(theme) {
    document.querySelectorAll('.theme-toggle-btn').forEach(btn => {
        const icon = btn.querySelector('i');
        const label = btn.querySelector('.theme-label');

        if (theme === 'light') {
            if (icon) icon.className = 'bi bi-moon-fill';
            if (label) label.textContent = 'Night Mode';
            btn.setAttribute('title', 'Switch to night theme');
        } else {
            if (icon) icon.className = 'bi bi-sun-fill';
            if (label) label.textContent = 'Day Mode';
            btn.setAttribute('title', 'Switch to day theme');
        }
    });
}

/**
 * Load saved theme when page opens
 */
function initTheme() {
    const saved = localStorage.getItem(THEME_KEY) || 'dark';
    applyTheme(saved);
}

// Setup toggle buttons after page loads
document.addEventListener('DOMContentLoaded', () => {
    initTheme();

    document.querySelectorAll('.theme-toggle-btn').forEach(btn => {
        btn.addEventListener('click', toggleTheme);
    });
});
