// Student Portal Controller

let currentStudent = null;
let currentRating = 5;
let currentMeals = [];

document.addEventListener('DOMContentLoaded', async () => {
    currentStudent = guardRoute('STUDENT');
    if (!currentStudent) return;

    setupStarRating();
    await loadStudentMeals();
    await loadStudentDashboard();
    await loadStudentHistory(0);
});

/**
 * Tab Switching Logic
 */
function switchStudentTab(tabName) {
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));

    const activeBtn = Array.from(document.querySelectorAll('.tab-btn')).find(b => b.textContent.toLowerCase().includes(tabName));
    if (activeBtn) activeBtn.classList.add('active');

    const activeContent = document.getElementById(`tab-${tabName}`);
    if (activeContent) activeContent.classList.add('active');

    if (tabName === 'dashboard') loadStudentDashboard();
    if (tabName === 'history') loadStudentHistory(0);
}

/**
 * Load Student Dashboard KPIs (GET /api/student/dashboard)
 */
async function loadStudentDashboard() {
    try {
        const stats = await apiRequest('/api/student/dashboard');
        document.getElementById('kpi-total-votes').textContent = stats.totalVotes || 0;
        document.getElementById('kpi-meals-claimed').textContent = stats.totalDeliveries || stats.mealsClaimed || 0;
        document.getElementById('kpi-active-tokens').textContent = stats.activeTokens || 0;
    } catch (err) {
        console.error('Error loading dashboard stats:', err);
    }
}

/**
 * Load Active & Upcoming Meals (GET /api/student/meals)
 */
async function loadStudentMeals() {
    const container = document.getElementById('meals-container');
    container.innerHTML = `<div style="text-align: center; padding: 2rem; color: var(--text-muted);">Loading meals...</div>`;

    try {
        currentMeals = await apiRequest('/api/student/meals');

        if (!currentMeals || currentMeals.length === 0) {
            container.innerHTML = `
                <div class="card" style="text-align: center; padding: 3rem;">
                    <span style="font-size: 3rem;">🍽️</span>
                    <h3 style="margin-top: 1rem;">No Scheduled Meals Found</h3>
                    <p style="color: var(--text-muted); font-size: 0.9rem; margin-top: 0.25rem;">
                        The hostel warden hasn't scheduled any upcoming meals yet. Check back soon!
                    </p>
                </div>`;
            return;
        }

        container.innerHTML = '';

        for (const meal of currentMeals) {
            // Check if student has already voted for this meal
            let existingVote = null;
            try {
                existingVote = await apiRequest(`/api/student/meals/${meal.id}/vote`);
            } catch (e) {
                // 404 means no vote cast yet, which is expected
            }

            const mealCard = renderMealCard(meal, existingVote);
            container.appendChild(mealCard);
        }
    } catch (err) {
        container.innerHTML = `<div class="card" style="color: var(--danger); text-align: center;">Failed to load meals: ${err.message}</div>`;
    }
}

/**
 * Render a Single Meal Card with Voting & QR Options
 */
function renderMealCard(meal, vote) {
    const card = document.createElement('div');
    card.className = 'meal-card';

    const statusBadgeClass = {
        'VOTING_OPEN': 'badge-open',
        'VOTING_CLOSED': 'badge-closed',
        'DELIVERY_OPEN': 'badge-active',
        'COMPLETED': 'badge-used',
        'UPCOMING': 'badge-closed'
    }[meal.status] || 'badge-closed';

    const isVotingOpen = meal.status === 'VOTING_OPEN';

    let dishesHtml = '';
    if (meal.foodOptions && meal.foodOptions.length > 0) {
        dishesHtml = meal.foodOptions.map(opt => {
            const isSelected = vote && vote.foodOptionId === opt.id;
            return `
                <div class="dish-choice-card ${isSelected ? 'selected' : ''}" onclick="selectDishForMeal(${meal.id}, ${opt.id})" id="dish-card-${meal.id}-${opt.id}">
                    <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                        <div class="dish-name">${opt.name}</div>
                        ${isSelected ? '<span class="badge badge-open">Your Choice ✓</span>' : ''}
                    </div>
                    <div class="dish-desc">${opt.description || 'Hostel Mess Fresh Preparation'}</div>
                </div>
            `;
        }).join('');
    } else {
        dishesHtml = `<div style="font-size: 0.85rem; color: var(--text-muted); padding: 0.5rem 0;">No food options added yet for this meal.</div>`;
    }

    card.innerHTML = `
        <div class="meal-header">
            <div>
                <div class="meal-type-title">
                    ${meal.mealType} — ${formatDate(meal.date)}
                </div>
                <div class="meal-meta">
                    ⏰ <strong>Voting Window:</strong> ${formatDateTime(meal.votingStartTime)} to ${formatDateTime(meal.votingEndTime)}
                </div>
                <div class="meal-meta">
                    🍱 <strong>Delivery Window:</strong> ${formatDateTime(meal.deliveryStartTime)} to ${formatDateTime(meal.deliveryEndTime)}
                </div>
            </div>
            <div>
                <span class="badge ${statusBadgeClass}">${meal.status.replace('_', ' ')}</span>
            </div>
        </div>

        <div style="font-size: 0.9rem; font-weight: 600; margin-top: 0.5rem; color: var(--text-secondary);">
            Available Dishes:
        </div>
        <div class="dishes-grid" id="dishes-grid-${meal.id}">
            ${dishesHtml}
        </div>

        <div style="display: flex; flex-wrap: wrap; gap: 0.75rem; margin-top: 1rem; align-items: center; justify-content: flex-end;">
            ${vote ? `
                ${isVotingOpen ? `
                    <button class="btn btn-outline btn-sm" onclick="cancelVote(${meal.id})">Cancel Vote</button>
                ` : ''}
                <button class="btn btn-primary btn-sm" onclick="openQrModal(${meal.id})">📱 View QR Meal Token</button>
                <button class="btn btn-outline btn-sm" onclick="openFeedbackModal(${meal.id}, '${meal.mealType}')">⭐ Rate Meal</button>
            ` : `
                ${isVotingOpen ? `
                    <button class="btn btn-primary btn-sm" onclick="submitSelectedVote(${meal.id})">🗳️ Submit Vote</button>
                ` : `
                    <span style="font-size: 0.8rem; color: var(--text-muted);">Voting is closed</span>
                `}
            `}
        </div>
    `;

    return card;
}

// Track temporary selected dish per meal
const selectedDishes = {};

function selectDishForMeal(mealId, foodOptionId) {
    selectedDishes[mealId] = foodOptionId;
    const grid = document.getElementById(`dishes-grid-${mealId}`);
    if (grid) {
        grid.querySelectorAll('.dish-choice-card').forEach(card => card.classList.remove('selected'));
        const target = document.getElementById(`dish-card-${mealId}-${foodOptionId}`);
        if (target) target.classList.add('selected');
    }
}

/**
 * Submit Vote (POST /api/student/meals/{mealId}/vote)
 */
async function submitSelectedVote(mealId) {
    const foodOptionId = selectedDishes[mealId];
    if (!foodOptionId) {
        showToast('Please select a dish to vote for.', 'warning');
        return;
    }

    try {
        await apiRequest(`/api/student/meals/${mealId}/vote`, {
            method: 'POST',
            body: { foodOptionId }
        });
        showToast('Vote recorded successfully!', 'success');
        await loadStudentMeals();
        await loadStudentDashboard();
    } catch (err) {
        showToast(err.message || 'Failed to submit vote', 'danger');
    }
}

/**
 * Cancel Vote (DELETE /api/student/meals/{mealId}/vote)
 */
async function cancelVote(mealId) {
    if (!confirm('Are you sure you want to cancel your vote?')) return;

    try {
        await apiRequest(`/api/student/meals/${mealId}/vote`, {
            method: 'DELETE'
        });
        showToast('Vote cancelled successfully.', 'info');
        await loadStudentMeals();
        await loadStudentDashboard();
    } catch (err) {
        showToast(err.message || 'Failed to cancel vote', 'danger');
    }
}

/**
 * Generate or View QR Meal Token (POST /api/student/meals/{mealId}/token)
 */
async function openQrModal(mealId) {
    try {
        const token = await apiRequest(`/api/student/meals/${mealId}/token`, {
            method: 'POST'
        });

        document.getElementById('qr-modal-image').src = token.qrCodeBase64;
        document.getElementById('qr-modal-code').textContent = token.tokenCode;
        document.getElementById('qr-modal-dish').textContent = token.foodName;
        document.getElementById('qr-modal-meta').textContent = `${token.hostel} | Room: ${token.roomNumber}`;
        
        const statusEl = document.getElementById('qr-modal-status');
        statusEl.textContent = token.status;
        statusEl.className = `badge badge-${token.status.toLowerCase()}`;

        openModal('qr-modal');
    } catch (err) {
        showToast(err.message || 'Failed to generate meal token', 'danger');
    }
}

/**
 * Download QR Code image as PNG
 */
function downloadStudentQrCode() {
    const img = document.getElementById('qr-modal-image');
    if (!img || !img.src) {
        showToast('No QR code image available to download', 'warning');
        return;
    }
    const tokenCode = document.getElementById('qr-modal-code')?.textContent?.trim() || 'token';
    const a = document.createElement('a');
    a.href = img.src;
    a.download = `MealToken-${tokenCode}.png`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    showToast('QR Code image downloaded!', 'success');
}

/**
 * Setup Interactive Star Rating
 */
function setupStarRating() {
    const box = document.getElementById('star-rating-box');
    const textEl = document.getElementById('star-rating-text');
    if (!box) return;

    const stars = box.querySelectorAll('span');
    stars.forEach(star => {
        star.addEventListener('click', () => {
            currentRating = parseInt(star.getAttribute('data-star'));
            updateStarDisplay(currentRating);
        });
    });

    updateStarDisplay(5);
}

function updateStarDisplay(rating) {
    const stars = document.querySelectorAll('#star-rating-box span');
    stars.forEach(s => {
        const starVal = parseInt(s.getAttribute('data-star'));
        if (starVal <= rating) {
            s.style.color = '#f59e0b';
        } else {
            s.style.color = '#cbd5e1';
        }
    });

    const labelMap = {
        1: '1 Star - Poor / Unsatisfied',
        2: '2 Stars - Below Average',
        3: '3 Stars - Average',
        4: '4 Stars - Good & Tasty',
        5: '5 Stars - Excellent Experience!'
    };
    document.getElementById('star-rating-text').textContent = labelMap[rating] || `${rating} Stars`;
}

/**
 * Open Feedback Modal
 */
function openFeedbackModal(mealId, mealType) {
    document.getElementById('feedback-meal-id').value = mealId;
    document.getElementById('feedback-meal-title').textContent = `Review for ${mealType}`;
    document.getElementById('feedback-comment').value = '';
    currentRating = 5;
    updateStarDisplay(5);
    openModal('feedback-modal');
}

/**
 * Submit Meal Feedback (POST /api/student/meals/{mealId}/feedback)
 */
async function submitMealFeedback() {
    const mealId = document.getElementById('feedback-meal-id').value;
    const category = document.getElementById('feedback-category').value;
    const comment = document.getElementById('feedback-comment').value.trim();

    try {
        await apiRequest(`/api/student/meals/${mealId}/feedback`, {
            method: 'POST',
            body: {
                rating: currentRating,
                category,
                comment: comment || null
            }
        });

        showToast('Thank you! Your feedback has been submitted.', 'success');
        closeModal('feedback-modal');
    } catch (err) {
        showToast(err.message || 'Failed to submit feedback. Ensure you have collected the meal.', 'danger');
    }
}

/**
 * Load Dining History (GET /api/student/history)
 */
async function loadStudentHistory(page = 0) {
    const tbody = document.getElementById('history-tbody');
    tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: var(--text-muted);">Loading history...</td></tr>`;

    try {
        const data = await apiRequest(`/api/student/history?page=${page}&size=10`);
        if (!data.content || data.content.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: var(--text-muted);">No history records found.</td></tr>`;
            return;
        }

        tbody.innerHTML = data.content.map(item => `
            <tr>
                <td>${formatDate(item.mealDate)}</td>
                <td><strong>${item.mealType}</strong></td>
                <td>${item.foodName}</td>
                <td>
                    <span class="badge ${item.delivered ? 'badge-open' : 'badge-closed'}">
                        ${item.delivered ? 'DELIVERED ✓' : 'VOTED ONLY'}
                    </span>
                </td>
                <td>${item.deliveredAt ? formatDateTime(item.deliveredAt) : '—'}</td>
            </tr>
        `).join('');

        // Render Pagination
        const pagDiv = document.getElementById('history-pagination');
        pagDiv.innerHTML = `
            <button class="btn btn-outline btn-sm" ${page === 0 ? 'disabled' : ''} onclick="loadStudentHistory(${page - 1})">Previous</button>
            <span style="font-size: 0.85rem; color: var(--text-muted);">Page ${data.pageNumber + 1} of ${data.totalPages || 1}</span>
            <button class="btn btn-outline btn-sm" ${data.last ? 'disabled' : ''} onclick="loadStudentHistory(${page + 1})">Next</button>
        `;
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="5" style="color: var(--danger); text-align: center;">Error loading history: ${err.message}</td></tr>`;
    }
}

/* --- Helpers --- */
function openModal(modalId) {
    const el = document.getElementById(modalId);
    if (el) el.classList.add('show');
}

function closeModal(modalId) {
    const el = document.getElementById(modalId);
    if (el) el.classList.remove('show');
}

function formatDate(dateStr) {
    if (!dateStr) return '—';
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

function formatDateTime(dateTimeStr) {
    if (!dateTimeStr) return '—';
    const d = new Date(dateTimeStr);
    return d.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
}
