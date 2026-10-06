// Authentication & Route Protection

/**
 * Handle Login Submission
 */
async function handleLogin(email, password) {
    try {
        const response = await apiRequest('/api/auth/login', {
            method: 'POST',
            body: { email, password }
        });

        localStorage.setItem('token', response.token);
        localStorage.setItem('user', JSON.stringify(response.user));

        showToast(`Welcome back, ${response.user.name}!`, 'success');

        setTimeout(() => {
            if (response.user.role === 'ADMIN') {
                window.location.href = '/admin.html';
            } else {
                window.location.href = '/student.html';
            }
        }, 600);
    } catch (err) {
        showToast(err.message || 'Invalid credentials', 'danger');
    }
}

/**
 * Get currently logged-in user from localStorage
 */
function getCurrentUser() {
    const userStr = localStorage.getItem('user');
    try {
        return userStr ? JSON.parse(userStr) : null;
    } catch {
        return null;
    }
}

/**
 * Enforce Route Access Guard
 */
function guardRoute(expectedRole) {
    const token = localStorage.getItem('token');
    const user = getCurrentUser();

    if (!token || !user) {
        window.location.href = '/index.html';
        return null;
    }

    if (expectedRole && user.role !== expectedRole) {
        showToast('Access denied: Unauthorized role.', 'danger');
        window.location.href = user.role === 'ADMIN' ? '/admin.html' : '/student.html';
        return null;
    }

    // Populate user details in top navbar if elements exist
    const nameEl = document.getElementById('nav-user-name');
    const roleEl = document.getElementById('nav-user-role');
    if (nameEl) nameEl.textContent = user.name;
    if (roleEl) roleEl.textContent = user.role === 'ADMIN' ? 'Hostel Administrator' : 'Hostel Student';

    return user;
}

/**
 * Log Out
 */
function logout() {
    localStorage.clear();
    showToast('Logged out successfully', 'info');
    setTimeout(() => {
        window.location.href = '/index.html';
    }, 400);
}
