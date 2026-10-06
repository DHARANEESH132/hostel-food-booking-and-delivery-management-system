// Centralized API Client & Toast System

const API_BASE = (window.location.port === '8080') ? '' : 'http://localhost:8080';

/**
 * Universal fetch wrapper that automatically handles:
 * - Bearer JWT Token attachment
 * - JSON headers and serialization
 * - Unified error message extraction from Spring GlobalExceptionHandler
 */
async function apiRequest(endpoint, options = {}) {
    const token = localStorage.getItem('token');
    
    const headers = {
        'Content-Type': 'application/json',
        ...(options.headers || {})
    };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const config = {
        ...options,
        headers
    };

    if (options.body && typeof options.body === 'object') {
        config.body = JSON.stringify(options.body);
    }

    try {
        const response = await fetch(`${API_BASE}${endpoint}`, config);

        if (response.status === 401) {
            // Unauthorized or token expired
            localStorage.clear();
            showToast('Session expired. Please log in again.', 'danger');
            setTimeout(() => {
                window.location.href = '/index.html';
            }, 1000);
            throw new Error('Unauthorized');
        }

        if (response.status === 204) {
            return null; // No content
        }

        const contentType = response.headers.get('content-type');
        const isJson = contentType && contentType.includes('application/json');
        const data = isJson ? await response.json() : await response.text();

        if (!response.ok) {
            const errorMessage = (data && data.message) || data.error || response.statusText || 'An error occurred';
            throw new Error(errorMessage);
        }

        return data;
    } catch (error) {
        throw error;
    }
}

/**
 * Global Toast Notification Generator
 * @param {string} message
 * @param {'success'|'danger'|'warning'|'info'} type
 * @param {number} duration
 */
function showToast(message, type = 'info', duration = 4000) {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    const icon = {
        success: '✓',
        danger: '✕',
        warning: '⚠',
        info: 'ℹ'
    }[type] || 'ℹ';

    toast.innerHTML = `<span><strong>${icon}</strong> &nbsp;${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, duration);
}
