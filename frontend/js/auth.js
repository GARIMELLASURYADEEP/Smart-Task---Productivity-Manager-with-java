/**
 * Authentication logic for login and signup pages.
 */

// Login form handler
const loginForm = document.getElementById('loginForm');
if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        hideAlert('alertBox');

        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value.trim();

        if (!username || !password) {
            showAlert('alertBox', 'Please enter username and password');
            return;
        }

        try {
            const { response, data } = await apiRequest('/api/auth/login', {
                method: 'POST',
                body: JSON.stringify({ username, password })
            });

            if (response.ok && data.success) {
                window.location.href = 'dashboard.html';
            } else {
                showAlert('alertBox', data.message || 'Login failed');
            }
        } catch (err) {
            showAlert('alertBox', 'Cannot connect to server. Start the Java backend first.');
        }
    });
}

// Signup form handler
const signupForm = document.getElementById('signupForm');
if (signupForm) {
    signupForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        hideAlert('alertBox');

        const username = document.getElementById('username').value.trim();
        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value.trim();
        const confirmPassword = document.getElementById('confirmPassword').value.trim();

        if (password !== confirmPassword) {
            showAlert('alertBox', 'Passwords do not match');
            return;
        }

        try {
            const { response, data } = await apiRequest('/api/auth/signup', {
                method: 'POST',
                body: JSON.stringify({ username, email, password })
            });

            if (response.ok && data.success) {
                window.location.href = 'dashboard.html';
            } else {
                showAlert('alertBox', data.message || 'Signup failed');
            }
        } catch (err) {
            showAlert('alertBox', 'Cannot connect to server. Start the Java backend first.');
        }
    });
}

// If already logged in, redirect to dashboard
(async function checkAlreadyLoggedIn() {
    if (window.location.pathname.includes('login') || window.location.pathname.includes('signup')) {
        try {
            const { response, data } = await apiRequest('/api/auth/me');
            if (response.ok && data.success) {
                window.location.href = 'dashboard.html';
            }
        } catch (e) {
            // Server not running - stay on login page
        }
    }
})();
