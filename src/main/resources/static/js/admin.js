// Admin & Mess Staff Portal Controller

let currentAdmin = null;
let allScheduledMeals = [];
let scannedTokenCode = null;

document.addEventListener('DOMContentLoaded', async () => {
    currentAdmin = guardRoute('ADMIN');
    if (!currentAdmin) return;

    initQrImageScanner();

    await loadAdminDashboard();
    await loadAdminMeals();
    await loadAdminStudents(0);
    await loadGlobalFeedbacks(0);
    await loadDeliveriesLog(0);

    // Set default datetime inputs for convenience
    setDefaultMealFormTimes();
});

/**
 * Tab Navigation
 */
function switchAdminTab(tabName) {
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));

    const activeBtn = Array.from(document.querySelectorAll('.tab-btn')).find(b => b.textContent.toLowerCase().includes(tabName));
    if (activeBtn) activeBtn.classList.add('active');

    const activeContent = document.getElementById(`tab-${tabName}`);
    if (activeContent) activeContent.classList.add('active');

    // Turn off camera if navigating away from scanner
    if (tabName !== 'scanner') {
        stopCameraScanner();
    }

    if (tabName === 'dashboard') loadAdminDashboard();
    if (tabName === 'meals') loadAdminMeals();
    if (tabName === 'students') loadAdminStudents(0);
    if (tabName === 'feedback') {
        loadFeedbackForSelectedMeal();
        loadGlobalFeedbacks(0);
    }
    if (tabName === 'deliveries') loadDeliveriesLog(0);
}

/**
 * 1. Dashboard KPIs (GET /api/admin/dashboard)
 */
async function loadAdminDashboard() {
    try {
        const stats = await apiRequest('/api/admin/dashboard');
        document.getElementById('kpi-total-students').textContent = stats.totalStudents || 0;
        document.getElementById('kpi-today-deliveries').textContent = stats.todayDeliveries || 0;
        document.getElementById('kpi-today-meals').textContent = stats.todayMeals || 0;
        document.getElementById('kpi-active-voting').textContent = stats.activeVotingMeals || 0;
    } catch (err) {
        console.error('Error loading dashboard stats:', err);
    }
}

/**
 * 2. QR Counter Scanner (Image File, Live Camera, Manual Input, and Confirmation)
 */
let currentScannerMode = 'image';
let html5FileDecoder = null;
let html5CameraScanner = null;
let isCameraRunning = false;

// Web Audio API feedback beep for physical scanner feel
function playScanSuccessBeep() {
    try {
        const AudioCtx = window.AudioContext || window.webkitAudioContext;
        if (!AudioCtx) return;
        const ctx = new AudioCtx();
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();
        osc.type = 'sine';
        osc.frequency.setValueAtTime(880, ctx.currentTime);
        osc.frequency.setValueAtTime(1175, ctx.currentTime + 0.08);
        gain.gain.setValueAtTime(0.2, ctx.currentTime);
        gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.25);
        osc.connect(gain);
        gain.connect(ctx.destination);
        osc.start();
        osc.stop(ctx.currentTime + 0.25);
    } catch (e) {
        // Audio playback not supported or user gesture required
    }
}

function setScannerMode(mode) {
    currentScannerMode = mode;
    ['image', 'camera', 'manual'].forEach(m => {
        const btn = document.getElementById(`btn-mode-${m}`);
        const sec = document.getElementById(`scanner-${m}-section`);
        if (btn) {
            if (m === mode) {
                btn.classList.remove('btn-outline');
                btn.classList.add('btn-primary');
            } else {
                btn.classList.remove('btn-primary');
                btn.classList.add('btn-outline');
            }
        }
        if (sec) {
            sec.style.display = (m === mode) ? 'block' : 'none';
        }
    });

    if (mode !== 'camera' && isCameraRunning) {
        stopCameraScanner();
    }
}

// --- QR Image Scanning (File Upload / Drag & Drop / Clipboard Paste) ---
function initQrImageScanner() {
    const dropzone = document.getElementById('qr-dropzone');
    if (!dropzone) return;

    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
        dropzone.addEventListener(eventName, (e) => {
            e.preventDefault();
            e.stopPropagation();
        }, false);
    });

    ['dragenter', 'dragover'].forEach(eventName => {
        dropzone.addEventListener(eventName, () => dropzone.classList.add('dragover'), false);
    });
    ['dragleave', 'drop'].forEach(eventName => {
        dropzone.addEventListener(eventName, () => dropzone.classList.remove('dragover'), false);
    });

    dropzone.addEventListener('drop', (e) => {
        const dt = e.dataTransfer;
        const files = dt?.files;
        if (files && files.length > 0) {
            handleQrImageFile(files[0]);
        }
    }, false);
}

function handleQrImageUpload(files) {
    if (!files || files.length === 0) return;
    handleQrImageFile(files[0]);
}

async function handleQrImageFile(file) {
    if (!file || !file.type.startsWith('image/')) {
        showToast('Please provide an image file (PNG, JPG, WEBP, etc.)', 'warning');
        return;
    }

    // Show image preview
    const previewBox = document.getElementById('image-scan-preview-box');
    const previewImg = document.getElementById('image-scan-preview');
    if (previewImg) previewImg.src = URL.createObjectURL(file);
    if (previewBox) previewBox.style.display = 'block';

    showToast('Decoding QR image...', 'info');

    try {
        if (!html5FileDecoder) {
            html5FileDecoder = new Html5Qrcode('qr-file-decoder');
        }

        const decodedText = await html5FileDecoder.scanFile(file, false);
        if (decodedText) {
            playScanSuccessBeep();
            showToast('QR code decoded successfully!', 'success');
            onQrCodeDetected(decodedText.trim());
        }
    } catch (err) {
        console.warn('QR decode failed:', err);
        showToast('Could not find a valid QR code in this image. Please ensure the QR is clear and unblurred.', 'danger');
    }
}

function resetImageScanner() {
    const fileInput = document.getElementById('qr-file-input');
    if (fileInput) fileInput.value = '';
    const previewBox = document.getElementById('image-scan-preview-box');
    if (previewBox) previewBox.style.display = 'none';
    clearScannedToken();
}

// --- Live Camera Scanner ---
async function startCameraScanner() {
    const readerContainer = document.getElementById('camera-scanner-view-container');
    const startBtn = document.getElementById('btn-start-camera');
    const stopBtn = document.getElementById('btn-stop-camera');
    const cameraSelect = document.getElementById('camera-select');

    try {
        if (!html5CameraScanner) {
            html5CameraScanner = new Html5Qrcode('camera-reader');
        }

        readerContainer.style.display = 'block';
        startBtn.style.display = 'none';
        stopBtn.style.display = 'inline-block';

        let cameraIdOrConfig = { facingMode: 'environment' };

        try {
            const cameras = await Html5Qrcode.getCameras();
            if (cameras && cameras.length > 0) {
                cameraSelect.style.display = cameras.length > 1 ? 'inline-block' : 'none';
                cameraSelect.innerHTML = cameras.map(cam => `<option value="${cam.id}">${cam.label || 'Camera ' + cam.id}</option>`).join('');
                if (cameraSelect.value) {
                    cameraIdOrConfig = cameraSelect.value;
                }
            }
        } catch (camErr) {
            console.warn('Could not enumerate cameras, falling back to facingMode:', camErr);
        }

        await html5CameraScanner.start(
            cameraIdOrConfig,
            {
                fps: 10,
                qrbox: { width: 250, height: 250 }
            },
            (decodedText) => {
                playScanSuccessBeep();
                stopCameraScanner();
                showToast('QR code captured via camera!', 'success');
                onQrCodeDetected(decodedText.trim());
            },
            () => {
                // Ignore per-frame non-detection
            }
        );

        isCameraRunning = true;
    } catch (err) {
        console.error('Camera start failed:', err);
        showToast('Camera error: Unable to start live scanner. Please check device permissions or use Image Upload.', 'danger');
        stopCameraScanner();
    }
}

async function stopCameraScanner() {
    if (html5CameraScanner && isCameraRunning) {
        try {
            await html5CameraScanner.stop();
        } catch (e) {
            console.warn('Error stopping camera:', e);
        }
    }
    isCameraRunning = false;
    const readerContainer = document.getElementById('camera-scanner-view-container');
    const startBtn = document.getElementById('btn-start-camera');
    const stopBtn = document.getElementById('btn-stop-camera');
    if (readerContainer) readerContainer.style.display = 'none';
    if (startBtn) startBtn.style.display = 'inline-block';
    if (stopBtn) stopBtn.style.display = 'none';
}

async function onCameraSelectChange() {
    if (isCameraRunning) {
        await stopCameraScanner();
        await startCameraScanner();
    }
}

// --- Process Detected QR Code ---
function onQrCodeDetected(tokenCode) {
    const indicator = document.getElementById('scanned-token-indicator');
    const badge = document.getElementById('detected-token-badge');
    if (indicator && badge) {
        badge.textContent = tokenCode;
        indicator.style.display = 'flex';
    }

    const manualInput = document.getElementById('scanner-token-input');
    if (manualInput) manualInput.value = tokenCode;

    executeScanVerification(tokenCode);
}

function clearScannedToken() {
    const indicator = document.getElementById('scanned-token-indicator');
    if (indicator) indicator.style.display = 'none';
    const manualInput = document.getElementById('scanner-token-input');
    if (manualInput) manualInput.value = '';
    const scanCard = document.getElementById('scan-result-card');
    if (scanCard) scanCard.style.display = 'none';
    scannedTokenCode = null;
}

// Global Clipboard Paste Listener for QR screenshots
window.addEventListener('paste', (e) => {
    const scannerTab = document.getElementById('tab-scanner');
    if (!scannerTab || !scannerTab.classList.contains('active')) return;

    const items = (e.clipboardData || window.clipboardData)?.items;
    if (!items) return;

    for (let item of items) {
        if (item.type.indexOf('image') !== -1) {
            const file = item.getAsFile();
            if (file) {
                setScannerMode('image');
                handleQrImageFile(file);
                break;
            }
        }
    }
});

// Verification API Call (POST /api/admin/delivery/scan)
async function executeScanVerification(tokenCode) {
    if (!tokenCode) return;

    try {
        const result = await apiRequest('/api/admin/delivery/scan', {
            method: 'POST',
            body: { tokenCode }
        });

        scannedTokenCode = result.tokenCode;
        document.getElementById('scan-student-name').textContent = result.studentName;
        document.getElementById('scan-student-id').textContent = `ID: ${result.studentRegistrationNumber || result.studentId}`;
        document.getElementById('scan-hostel-room').textContent = `${result.hostel} / Room ${result.roomNumber}`;
        document.getElementById('scan-dish-name').textContent = result.foodName;

        const badge = document.getElementById('scan-status-badge');
        badge.innerHTML = `<span class="badge ${result.valid ? 'badge-open' : 'badge-expired'}">${result.status}</span>`;

        const confirmBtn = document.getElementById('confirm-delivery-btn');
        confirmBtn.disabled = !result.valid;

        document.getElementById('scan-result-card').style.display = 'block';
        showToast(result.message, result.valid ? 'success' : 'warning');
    } catch (err) {
        showToast(err.message || 'Token not found or invalid', 'danger');
        document.getElementById('scan-result-card').style.display = 'none';
    }
}

// Fallback manual form submit
async function scanQrToken() {
    const input = document.getElementById('scanner-token-input');
    const tokenCode = input.value.trim();
    if (!tokenCode) return;
    onQrCodeDetected(tokenCode);
}

// Confirm Meal Handover (POST /api/admin/delivery/confirm)
async function confirmDeliveryAction() {
    if (!scannedTokenCode) return;
    const notes = document.getElementById('delivery-notes').value.trim();

    try {
        await apiRequest('/api/admin/delivery/confirm', {
            method: 'POST',
            body: {
                tokenCode: scannedTokenCode,
                notes: notes || null
            }
        });

        showToast('Meal successfully handed over & recorded!', 'success');
        clearScannedToken();
        resetImageScanner();
        await loadAdminDashboard();
    } catch (err) {
        showToast(err.message || 'Failed to confirm delivery', 'danger');
    }
}

/**
 * 3. Meals & Menu Management (GET /api/admin/meals, POST /api/admin/meals, DELETE /api/admin/meals/{id})
 */
async function loadAdminMeals() {
    const container = document.getElementById('admin-meals-container');
    container.innerHTML = `<div style="text-align: center; color: var(--text-muted); padding: 2rem;">Loading meals...</div>`;

    try {
        allScheduledMeals = await apiRequest('/api/admin/meals');
        populateMealSelectDropdowns(allScheduledMeals);

        if (!allScheduledMeals || allScheduledMeals.length === 0) {
            container.innerHTML = `<div style="text-align: center; padding: 2rem; color: var(--text-muted);">No meals scheduled yet.</div>`;
            return;
        }

        container.innerHTML = allScheduledMeals.map(meal => {
            const dishesHtml = (meal.foodOptions && meal.foodOptions.length > 0)
                ? meal.foodOptions.map(opt => `
                    <div style="display: flex; justify-content: space-between; align-items: center; background: #f8fafc; padding: 0.5rem 0.75rem; border-radius: 6px; margin-bottom: 0.35rem;">
                        <div>
                            <strong>${opt.name}</strong>
                            ${opt.description ? `<span style="font-size: 0.8rem; color: var(--text-muted);"> — ${opt.description}</span>` : ''}
                        </div>
                        <button class="btn btn-danger btn-sm" onclick="deleteDish(${opt.id})" title="Delete Dish">✕</button>
                    </div>
                `).join('')
                : `<div style="font-size: 0.85rem; color: var(--text-muted);">No dishes added yet.</div>`;

            return `
                <div class="card" style="margin-bottom: 1rem;">
                    <div class="card-header">
                        <div>
                            <h4 style="font-size: 1.1rem; font-weight: 700;">${meal.mealType} (${formatDate(meal.date)})</h4>
                            <div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 0.2rem;">
                                Voting: ${formatDateTime(meal.votingStartTime)} - ${formatDateTime(meal.votingEndTime)} | 
                                Delivery: ${formatDateTime(meal.deliveryStartTime)} - ${formatDateTime(meal.deliveryEndTime)}
                            </div>
                        </div>
                        <div style="display: flex; gap: 0.5rem; align-items: center;">
                            <span class="badge badge-open">${meal.status}</span>
                            <button class="btn btn-outline btn-sm" onclick="openAddDishModal(${meal.id})">➕ Add Dish</button>
                            <button class="btn btn-danger btn-sm" onclick="deleteMeal(${meal.id})">🗑️ Delete</button>
                        </div>
                    </div>
                    <div>
                        <div style="font-size: 0.85rem; font-weight: 600; margin-bottom: 0.5rem; color: var(--text-secondary);">Dishes:</div>
                        ${dishesHtml}
                    </div>
                </div>
            `;
        }).join('');
    } catch (err) {
        container.innerHTML = `<div style="color: var(--danger);">Failed to load meals: ${err.message}</div>`;
    }
}

function populateMealSelectDropdowns(meals) {
    const selects = [
        document.getElementById('demand-meal-select'),
        document.getElementById('feedback-meal-select'),
        document.getElementById('reports-meal-select')
    ];

    selects.forEach(sel => {
        if (!sel) return;
        const currentVal = sel.value;
        sel.innerHTML = `<option value="">-- Choose Meal --</option>` +
            meals.map(m => `<option value="${m.id}">${m.mealType} - ${formatDate(m.date)}</option>`).join('');
        if (currentVal) sel.value = currentVal;
    });
}

async function handleCreateMeal() {
    const date = document.getElementById('meal-date').value;
    const mealType = document.getElementById('meal-type').value;
    const votingStartTime = document.getElementById('voting-start').value;
    const votingEndTime = document.getElementById('voting-end').value;
    const deliveryStartTime = document.getElementById('delivery-start').value;
    const deliveryEndTime = document.getElementById('delivery-end').value;

    try {
        await apiRequest('/api/admin/meals', {
            method: 'POST',
            body: {
                date,
                mealType,
                votingStartTime,
                votingEndTime,
                deliveryStartTime,
                deliveryEndTime
            }
        });

        showToast('Meal scheduled successfully!', 'success');
        await loadAdminMeals();
        await loadAdminDashboard();
    } catch (err) {
        showToast(err.message || 'Failed to schedule meal', 'danger');
    }
}

async function deleteMeal(mealId) {
    if (!confirm('Are you sure you want to delete this meal?')) return;
    try {
        await apiRequest(`/api/admin/meals/${mealId}`, { method: 'DELETE' });
        showToast('Meal deleted successfully.', 'info');
        await loadAdminMeals();
        await loadAdminDashboard();
    } catch (err) {
        showToast(err.message || 'Cannot delete meal with existing records.', 'danger');
    }
}

function openAddDishModal(mealId) {
    document.getElementById('modal-dish-meal-id').value = mealId;
    document.getElementById('modal-dish-name').value = '';
    document.getElementById('modal-dish-desc').value = '';
    openModal('add-dish-modal');
}

async function handleAddDishSubmit() {
    const mealId = document.getElementById('modal-dish-meal-id').value;
    const name = document.getElementById('modal-dish-name').value.trim();
    const description = document.getElementById('modal-dish-desc').value.trim();

    if (!name) {
        showToast('Dish name is required', 'warning');
        return;
    }

    try {
        await apiRequest(`/api/admin/meals/${mealId}/food-options`, {
            method: 'POST',
            body: { name, description: description || null }
        });
        showToast('Dish added successfully!', 'success');
        closeModal('add-dish-modal');
        await loadAdminMeals();
    } catch (err) {
        showToast(err.message || 'Failed to add dish', 'danger');
    }
}

async function deleteDish(dishId) {
    if (!confirm('Delete this food option?')) return;
    try {
        await apiRequest(`/api/admin/food-options/${dishId}`, { method: 'DELETE' });
        showToast('Dish removed.', 'info');
        await loadAdminMeals();
    } catch (err) {
        showToast(err.message || 'Cannot delete dish with active votes.', 'danger');
    }
}

/**
 * 4. Kitchen Demand Forecasting (GET /api/admin/meals/{id}/demand)
 */
async function loadDemandForSelectedMeal() {
    const mealId = document.getElementById('demand-meal-select').value;
    const resultsDiv = document.getElementById('demand-results');
    if (!mealId) {
        resultsDiv.innerHTML = `<div style="text-align: center; color: var(--text-muted); padding: 2rem;">Select a meal to view demand forecasting.</div>`;
        return;
    }

    resultsDiv.innerHTML = `<div style="text-align: center; color: var(--text-muted); padding: 1rem;">Calculating demand portions...</div>`;

    try {
        const demand = await apiRequest(`/api/admin/meals/${mealId}/demand`);

        let barsHtml = '';
        if (demand.optionsDemand && demand.optionsDemand.length > 0) {
            barsHtml = demand.optionsDemand.map(opt => `
                <div class="demand-bar-container">
                    <div class="demand-label">
                        <span><strong>${opt.foodOptionName}</strong></span>
                        <span>${opt.voteCount} portions (${opt.percentage.toFixed(1)}%)</span>
                    </div>
                    <div class="progress-track">
                        <div class="progress-fill" style="width: ${opt.percentage}%;"></div>
                    </div>
                </div>
            `).join('');
        } else {
            barsHtml = `<div style="color: var(--text-muted); font-size: 0.9rem;">No votes recorded yet for this meal.</div>`;
        }

        resultsDiv.innerHTML = `
            <div style="background: #f8fafc; border-radius: 8px; padding: 1.25rem; border: 1px solid var(--border-color);">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
                    <span style="font-size: 1.1rem; font-weight: 700;">Total Pre-meal Orders: ${demand.totalVotes}</span>
                    <span class="badge badge-open">Demand Locked</span>
                </div>
                ${barsHtml}
            </div>
        `;
    } catch (err) {
        resultsDiv.innerHTML = `<div style="color: var(--danger);">Failed to calculate demand: ${err.message}</div>`;
    }
}

/**
 * 5. Student Roster & Onboarding (POST /api/admin/students, GET /api/admin/students, DELETE /api/admin/students/{id})
 */
async function handleOnboardStudent() {
    const name = document.getElementById('st-name').value.trim();
    const studentId = document.getElementById('st-id').value.trim();
    const email = document.getElementById('st-email').value.trim();
    const hostel = document.getElementById('st-hostel').value.trim();
    const roomNumber = document.getElementById('st-room').value.trim();
    const password = document.getElementById('st-password').value.trim();

    try {
        await apiRequest('/api/admin/students', {
            method: 'POST',
            body: {
                name,
                studentId,
                email,
                hostel,
                roomNumber,
                password: password || null
            }
        });

        showToast(`Student ${name} onboarded successfully!`, 'success');
        document.getElementById('create-student-form').reset();
        await loadAdminStudents(0);
        await loadAdminDashboard();
    } catch (err) {
        showToast(err.message || 'Failed to onboard student', 'danger');
    }
}

async function loadAdminStudents(page = 0) {
    const tbody = document.getElementById('students-tbody');
    tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: var(--text-muted);">Loading students...</td></tr>`;

    try {
        const data = await apiRequest(`/api/admin/students?page=${page}&size=10`);
        if (!data.content || data.content.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: var(--text-muted);">No students found.</td></tr>`;
            return;
        }

        tbody.innerHTML = data.content.map(s => `
            <tr>
                <td><strong>${s.studentId}</strong></td>
                <td>${s.name}</td>
                <td>${s.email}</td>
                <td>${s.hostel}</td>
                <td>${s.roomNumber}</td>
                <td>
                    <button class="btn btn-danger btn-sm" onclick="deleteStudentAccount(${s.id}, '${s.name}')">Revoke</button>
                </td>
            </tr>
        `).join('');

        const pagDiv = document.getElementById('students-pagination');
        pagDiv.innerHTML = `
            <button class="btn btn-outline btn-sm" ${page === 0 ? 'disabled' : ''} onclick="loadAdminStudents(${page - 1})">Previous</button>
            <span style="font-size: 0.85rem; color: var(--text-muted);">Page ${data.pageNumber + 1} of ${data.totalPages || 1}</span>
            <button class="btn btn-outline btn-sm" ${data.last ? 'disabled' : ''} onclick="loadAdminStudents(${page + 1})">Next</button>
        `;
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="6" style="color: var(--danger); text-align: center;">Error: ${err.message}</td></tr>`;
    }
}

async function deleteStudentAccount(id, name) {
    if (!confirm(`Revoke account for ${name}?`)) return;
    try {
        await apiRequest(`/api/admin/students/${id}`, { method: 'DELETE' });
        showToast('Student account revoked.', 'info');
        await loadAdminStudents(0);
        await loadAdminDashboard();
    } catch (err) {
        showToast(err.message || 'Cannot delete student with historical records.', 'danger');
    }
}

/**
 * 6. Quality Feedback & Analytics (GET /api/admin/meals/{id}/feedback & GET /api/admin/feedbacks)
 */
async function loadFeedbackForSelectedMeal() {
    const mealId = document.getElementById('feedback-meal-select').value;
    const resultsDiv = document.getElementById('feedback-results');
    if (!mealId) {
        resultsDiv.innerHTML = `<div style="text-align: center; color: var(--text-muted); padding: 2rem;">Select a meal to view feedback analytics.</div>`;
        return;
    }

    try {
        const summary = await apiRequest(`/api/admin/meals/${mealId}/feedback`);

        const starRows = [5, 4, 3, 2, 1].map(s => {
            const count = (summary.starDistribution && summary.starDistribution[s]) || 0;
            const pct = summary.totalFeedbacks > 0 ? (count / summary.totalFeedbacks) * 100 : 0;
            return `
                <div style="display: flex; align-items: center; gap: 0.5rem; font-size: 0.85rem; margin-bottom: 0.25rem;">
                    <span style="width: 35px;">${s} ★</span>
                    <div style="flex: 1; background: #e2e8f0; border-radius: 4px; height: 8px; overflow: hidden;">
                        <div style="width: ${pct}%; background: #f59e0b; height: 100%;"></div>
                    </div>
                    <span style="width: 35px; text-align: right; color: var(--text-muted);">${count}</span>
                </div>
            `;
        }).join('');

        const reviewsHtml = (summary.feedbacks && summary.feedbacks.content && summary.feedbacks.content.length > 0)
            ? summary.feedbacks.content.map(fb => `
                <div style="border-bottom: 1px solid var(--border-color); padding: 0.75rem 0;">
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                        <div>
                            <strong>${fb.studentName}</strong> (${fb.studentId}) — <span style="color: #f59e0b;">${'★'.repeat(fb.rating)}</span>
                        </div>
                        <span class="badge badge-student">${fb.category}</span>
                    </div>
                    <div style="font-size: 0.85rem; color: var(--text-secondary); margin-top: 0.25rem;">
                        ${fb.comment || '<em>No written comment</em>'}
                    </div>
                </div>
            `).join('')
            : `<div style="color: var(--text-muted); padding: 0.5rem 0;">No written reviews submitted yet.</div>`;

        resultsDiv.innerHTML = `
            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 1.5rem; margin-bottom: 1.5rem;">
                <div style="background: #f8fafc; padding: 1.25rem; border-radius: 8px; border: 1px solid var(--border-color); text-align: center;">
                    <div style="font-size: 2.5rem; font-weight: 800; color: var(--primary);">${summary.averageRating.toFixed(1)}</div>
                    <div style="color: #f59e0b; font-size: 1.25rem; margin: 0.25rem 0;">★★★★★</div>
                    <div style="font-size: 0.85rem; color: var(--text-muted);">${summary.totalFeedbacks} Total Student Reviews</div>
                </div>
                <div style="background: #f8fafc; padding: 1.25rem; border-radius: 8px; border: 1px solid var(--border-color);">
                    <div style="font-size: 0.9rem; font-weight: 600; margin-bottom: 0.5rem;">Star Breakdown</div>
                    ${starRows}
                </div>
            </div>
            <div style="margin-top: 1rem;">
                <h4 style="font-size: 1rem; font-weight: 700; margin-bottom: 0.75rem;">Student Comments</h4>
                ${reviewsHtml}
            </div>
        `;
    } catch (err) {
        resultsDiv.innerHTML = `<div style="color: var(--danger);">Failed to load feedback: ${err.message}</div>`;
    }
}

async function loadGlobalFeedbacks(page = 0) {
    const tbody = document.getElementById('global-feedback-tbody');
    tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--text-muted);">Loading global feedback...</td></tr>`;

    try {
        const data = await apiRequest(`/api/admin/feedbacks?page=${page}&size=10`);
        if (!data.content || data.content.length === 0) {
            tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--text-muted);">No feedback records found.</td></tr>`;
            return;
        }

        tbody.innerHTML = data.content.map(fb => `
            <tr>
                <td><strong>${fb.mealType || 'MEAL'}</strong></td>
                <td>${fb.studentName}</td>
                <td>${fb.foodOptionName || 'Dish'}</td>
                <td style="color: #f59e0b;">${'★'.repeat(fb.rating)}</td>
                <td><span class="badge badge-student">${fb.category}</span></td>
                <td>${fb.comment || '—'}</td>
                <td>${formatDate(fb.createdAt)}</td>
            </tr>
        `).join('');

        const pagDiv = document.getElementById('global-feedback-pagination');
        pagDiv.innerHTML = `
            <button class="btn btn-outline btn-sm" ${page === 0 ? 'disabled' : ''} onclick="loadGlobalFeedbacks(${page - 1})">Previous</button>
            <span style="font-size: 0.85rem; color: var(--text-muted);">Page ${data.pageNumber + 1} of ${data.totalPages || 1}</span>
            <button class="btn btn-outline btn-sm" ${data.last ? 'disabled' : ''} onclick="loadGlobalFeedbacks(${page + 1})">Next</button>
        `;
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="7" style="color: var(--danger); text-align: center;">Error: ${err.message}</td></tr>`;
    }
}

/**
 * 7. Wastage & Consumption Reports (GET /api/admin/reports/meals/{id})
 */
async function loadWastageReportForMeal() {
    const mealId = document.getElementById('reports-meal-select').value;
    const resultsDiv = document.getElementById('report-results');
    if (!mealId) {
        resultsDiv.innerHTML = `<div style="text-align: center; color: var(--text-muted); padding: 2rem;">Select a meal to view consumption vs waste.</div>`;
        return;
    }

    try {
        const report = await apiRequest(`/api/admin/reports/meals/${mealId}`);

        resultsDiv.innerHTML = `
            <div class="kpi-grid" style="margin-top: 1rem;">
                <div class="kpi-card">
                    <div class="kpi-icon icon-blue">🍳</div>
                    <div>
                        <div class="kpi-title">Portions Prepared</div>
                        <div class="kpi-value">${report.totalPrepared || report.totalVotes || 0}</div>
                    </div>
                </div>
                <div class="kpi-card">
                    <div class="kpi-icon icon-green">🍽️</div>
                    <div>
                        <div class="kpi-title">Portions Consumed</div>
                        <div class="kpi-value">${report.totalDelivered || 0}</div>
                    </div>
                </div>
                <div class="kpi-card">
                    <div class="kpi-icon icon-amber">📊</div>
                    <div>
                        <div class="kpi-title">Consumption Rate</div>
                        <div class="kpi-value">${((report.consumptionRate || 0)).toFixed(1)}%</div>
                    </div>
                </div>
                <div class="kpi-card">
                    <div class="kpi-icon icon-rose">🗑️</div>
                    <div>
                        <div class="kpi-title">Portions Wasted</div>
                        <div class="kpi-value">${report.wastedCount || 0}</div>
                    </div>
                </div>
            </div>
        `;
    } catch (err) {
        resultsDiv.innerHTML = `<div style="color: var(--danger);">Failed to load report: ${err.message}</div>`;
    }
}

/**
 * 8. Completed Delivery Logs Audit (GET /api/admin/deliveries)
 */
async function loadDeliveriesLog(page = 0) {
    const tbody = document.getElementById('deliveries-tbody');
    tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: var(--text-muted);">Loading deliveries...</td></tr>`;

    try {
        const data = await apiRequest(`/api/admin/deliveries?page=${page}&size=10`);
        if (!data.content || data.content.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: var(--text-muted);">No delivery logs found.</td></tr>`;
            return;
        }

        tbody.innerHTML = data.content.map(d => `
            <tr>
                <td><span class="token-string" style="font-size: 0.8rem;">${d.tokenCode}</span></td>
                <td>${d.studentName}</td>
                <td>${d.hostel} / ${d.roomNumber}</td>
                <td><strong>${d.mealType}</strong></td>
                <td>${d.foodName}</td>
                <td>${d.deliveredByAdmin || 'Staff'}</td>
                <td>${formatDateTime(d.deliveredAt)}</td>
                <td>${d.notes || '—'}</td>
            </tr>
        `).join('');

        const pagDiv = document.getElementById('deliveries-pagination');
        pagDiv.innerHTML = `
            <button class="btn btn-outline btn-sm" ${page === 0 ? 'disabled' : ''} onclick="loadDeliveriesLog(${page - 1})">Previous</button>
            <span style="font-size: 0.85rem; color: var(--text-muted);">Page ${data.pageNumber + 1} of ${data.totalPages || 1}</span>
            <button class="btn btn-outline btn-sm" ${data.last ? 'disabled' : ''} onclick="loadDeliveriesLog(${page + 1})">Next</button>
        `;
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="8" style="color: var(--danger); text-align: center;">Error: ${err.message}</td></tr>`;
    }
}

/* --- Helpers --- */
function setDefaultMealFormTimes() {
    const today = new Date();
    const pad = (n) => String(n).padStart(2, '0');
    const yyyy = today.getFullYear();
    const mm = pad(today.getMonth() + 1);
    const dd = pad(today.getDate());

    const dateInput = document.getElementById('meal-date');
    if (dateInput) dateInput.value = `${yyyy}-${mm}-${dd}`;

    const vStart = document.getElementById('voting-start');
    const vEnd = document.getElementById('voting-end');
    const dStart = document.getElementById('delivery-start');
    const dEnd = document.getElementById('delivery-end');

    if (vStart) vStart.value = `${yyyy}-${mm}-${dd}T06:00`;
    if (vEnd) vEnd.value = `${yyyy}-${mm}-${dd}T10:00`;
    if (dStart) dStart.value = `${yyyy}-${mm}-${dd}T12:00`;
    if (dEnd) dEnd.value = `${yyyy}-${mm}-${dd}T14:30`;
}

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
