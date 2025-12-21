// Check authentication on page load
requireAuth();

let allPurchases = [];
let currentFilter = 'all';
let selectedPurchaseId = null;

// Load all purchases on page load
document.addEventListener('DOMContentLoaded', function() {
    loadPurchases();

    // Add filter event listeners
    document.querySelectorAll('input[name="filterStatus"]').forEach(radio => {
        radio.addEventListener('change', function() {
            currentFilter = this.value;
            renderPurchases();
        });
    });
});

// Load all voice purchases
async function loadPurchases() {
    try {
        const endpoint = '/voice-purchase';
        const response = await apiRequest(endpoint);

        if (!response.ok) {
            throw new Error('Failed to load voice purchases');
        }

        const data = await response.json();
        allPurchases = data.data || [];
        updateStatistics();
        renderPurchases();
    } catch (error) {
        console.error('Error loading purchases:', error);
        showAlert('Failed to load voice purchases: ' + error.message, 'danger');
        document.getElementById('purchasesTable').innerHTML = `
            <tr><td colspan="9" class="text-center text-danger">Failed to load purchases</td></tr>
        `;
    }
}

// Update statistics cards
function updateStatistics() {
    const pending = allPurchases.filter(p => p.status === 'pending').length;
    const approved = allPurchases.filter(p => p.status === 'approved').length;
    const rejected = allPurchases.filter(p => p.status === 'rejected').length;

    document.getElementById('pendingCount').textContent = pending;
    document.getElementById('approvedCount').textContent = approved;
    document.getElementById('rejectedCount').textContent = rejected;
    document.getElementById('totalCount').textContent = allPurchases.length;
}

// Render purchases table based on current filter
function renderPurchases() {
    const filteredPurchases = currentFilter === 'all'
        ? allPurchases
        : allPurchases.filter(p => p.status === currentFilter);

    const tableBody = document.getElementById('purchasesTable');

    if (filteredPurchases.length === 0) {
        tableBody.innerHTML = `
            <tr><td colspan="9" class="text-center">No ${currentFilter === 'all' ? '' : currentFilter} purchases found</td></tr>
        `;
        return;
    }

    tableBody.innerHTML = filteredPurchases.map(purchase => `
        <tr>
            <td>${purchase.id}</td>
            <td>
                <div>${purchase.user ? purchase.user.username : 'N/A'}</div>
                <small class="text-muted">${purchase.user ? `${purchase.user.firstName || ''} ${purchase.user.lastName || ''}` : ''}</small>
            </td>
            <td>
                <div><strong>${purchase.voiceType ? purchase.voiceType.voiceName : 'Unknown'}</strong></div>
                <small class="text-muted">Code: ${purchase.voiceType ? purchase.voiceType.code : 'N/A'}</small>
            </td>
            <td>${purchase.transactionMethod || 'N/A'}</td>
            <td>
                <code>${purchase.tnxId || 'N/A'}</code>
            </td>
            <td>${formatCurrency(purchase.amount)} BDT</td>
            <td>${getStatusBadge(purchase.status)}</td>
            <td>
                <div>${formatDate(purchase.purchaseDate)}</div>
                ${purchase.updatedAt ? `<small class="text-muted">Updated: ${formatDate(purchase.updatedAt)}</small>` : ''}
            </td>
            <td>
                <button class="btn btn-sm btn-info" onclick="viewPurchaseDetails(${purchase.id})">
                    <i class="bi bi-eye"></i>
                </button>
                ${purchase.status === 'pending' ? `
                    <button class="btn btn-sm btn-success" onclick="approvePurchase(${purchase.id})" title="Approve">
                        <i class="bi bi-check-circle"></i>
                    </button>
                    <button class="btn btn-sm btn-danger" onclick="rejectPurchase(${purchase.id})" title="Reject">
                        <i class="bi bi-x-circle"></i>
                    </button>
                ` : ''}
            </td>
        </tr>
    `).join('');
}

// Get status badge HTML
function getStatusBadge(status) {
    const badges = {
        'pending': '<span class="badge bg-warning">Pending</span>',
        'approved': '<span class="badge bg-success">Approved</span>',
        'rejected': '<span class="badge bg-danger">Rejected</span>'
    };
    return badges[status] || '<span class="badge bg-secondary">Unknown</span>';
}

// View purchase details
function viewPurchaseDetails(purchaseId) {
    const purchase = allPurchases.find(p => p.id === purchaseId);
    if (!purchase) {
        showAlert('Purchase not found', 'danger');
        return;
    }

    selectedPurchaseId = purchaseId;

    // Fill modal with purchase details
    document.getElementById('detailPurchaseId').textContent = purchase.id;
    document.getElementById('detailVoiceType').textContent = purchase.voiceType ? `${purchase.voiceType.voiceName} (${purchase.voiceType.code})` : 'N/A';
    document.getElementById('detailAmount').textContent = formatCurrency(purchase.amount) + ' BDT';
    document.getElementById('detailStatus').innerHTML = getStatusBadge(purchase.status);
    document.getElementById('detailPurchaseDate').textContent = formatDate(purchase.purchaseDate);
    document.getElementById('detailUpdatedAt').textContent = purchase.updatedAt ? formatDate(purchase.updatedAt) : 'N/A';

    document.getElementById('detailUserId').textContent = purchase.user ? purchase.user.id : purchase.idUser;
    document.getElementById('detailUsername').textContent = purchase.user ? purchase.user.username : 'N/A';
    document.getElementById('detailUserName').textContent = purchase.user ? `${purchase.user.firstName || ''} ${purchase.user.lastName || ''}` : 'N/A';

    document.getElementById('detailPaymentMethod').textContent = purchase.transactionMethod || 'N/A';
    document.getElementById('detailTnxId').innerHTML = `<code>${purchase.tnxId || 'N/A'}</code>`;

    // Show/hide approve/reject buttons based on status
    const approveBtn = document.getElementById('approveBtn');
    const rejectBtn = document.getElementById('rejectBtn');
    if (purchase.status === 'pending') {
        approveBtn.style.display = 'inline-block';
        rejectBtn.style.display = 'inline-block';
    } else {
        approveBtn.style.display = 'none';
        rejectBtn.style.display = 'none';
    }

    // Show modal
    const modal = new bootstrap.Modal(document.getElementById('purchaseDetailsModal'));
    modal.show();
}

// Approve purchase
async function approvePurchase(purchaseId) {
    if (!confirm('Are you sure you want to approve this voice purchase? The user will get permanent access to this voice type.')) {
        return;
    }

    try {
        const response = await apiRequest(`/voice-purchase/${purchaseId}/approve`, {
            method: 'PUT'
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Failed to approve purchase');
        }

        showAlert('Voice purchase approved successfully! User has been granted access.', 'success');
        await loadPurchases();
    } catch (error) {
        console.error('Error approving purchase:', error);
        showAlert('Failed to approve purchase: ' + error.message, 'danger');
    }
}

// Reject purchase
async function rejectPurchase(purchaseId) {
    if (!confirm('Are you sure you want to reject this voice purchase?')) {
        return;
    }

    try {
        const response = await apiRequest(`/voice-purchase/${purchaseId}/reject`, {
            method: 'PUT'
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Failed to reject purchase');
        }

        showAlert('Voice purchase rejected successfully.', 'info');
        await loadPurchases();
    } catch (error) {
        console.error('Error rejecting purchase:', error);
        showAlert('Failed to reject purchase: ' + error.message, 'danger');
    }
}

// Approve purchase from modal
async function approvePurchaseFromModal() {
    if (selectedPurchaseId) {
        await approvePurchase(selectedPurchaseId);
        const modal = bootstrap.Modal.getInstance(document.getElementById('purchaseDetailsModal'));
        modal.hide();
    }
}

// Reject purchase from modal
async function rejectPurchaseFromModal() {
    if (selectedPurchaseId) {
        await rejectPurchase(selectedPurchaseId);
        const modal = bootstrap.Modal.getInstance(document.getElementById('purchaseDetailsModal'));
        modal.hide();
    }
}
