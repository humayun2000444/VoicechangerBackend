document.addEventListener('DOMContentLoaded', async function() {
    if (!requireAuth()) return;
    await loadUsers();
});

async function loadUsers() {
    try {
        const response = await apiRequest('/users');
        const data = await response.json();
        const tableBody = document.getElementById('usersTable');
        
        if (!data.data || data.data.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="8" class="text-center">No users found</td></tr>';
            return;
        }

        tableBody.innerHTML = data.data.map(u => {
            const statusBadge = u.enabled ? 
                '<span class="badge bg-success">Active</span>' : 
                '<span class="badge bg-secondary">Disabled</span>';
            const roles = u.roles ? u.roles.join(', ') : 'N/A';
            
            return `
                <tr>
                    <td>${u.id}</td>
                    <td>${u.username}</td>
                    <td>${u.firstName} ${u.lastName}</td>
                    <td><small>${roles}</small></td>
                    <td>${statusBadge}</td>
                    <td>
                        <button class="btn btn-sm btn-info" onclick="viewBalance(${u.id}, '${u.username}')">
                            <i class="bi bi-wallet2"></i> Balance
                        </button>
                    </td>
                    <td><small>${formatDate(u.createdAt)}</small></td>
                    <td>
                        <button class="btn btn-sm btn-${u.enabled ? 'warning' : 'success'}" 
                                onclick="toggleUserStatus(${u.id}, ${u.enabled})">
                            <i class="bi bi-${u.enabled ? 'x-circle' : 'check-circle'}"></i>
                        </button>
                        <button class="btn btn-sm btn-danger" onclick="deleteUser(${u.id}, '${u.username}')">
                            <i class="bi bi-trash"></i>
                        </button>
                    </td>
                </tr>
            `;
        }).join('');
    } catch (error) {
        console.error('Error loading users:', error);
        document.getElementById('usersTable').innerHTML = '<tr><td colspan="8" class="text-center text-danger">Error loading users</td></tr>';
    }
}

async function viewBalance(userId, username) {
    try {
        const response = await apiRequest(`/balance/user/${userId}`);
        const data = await response.json();
        const balance = data.data;

        const balanceDetails = document.getElementById('balanceDetails');
        balanceDetails.innerHTML = `
            <div class="mb-3">
                <h6>User: <strong>${username}</strong></h6>
            </div>
            <table class="table table-sm">
                <tr>
                    <td>Total Purchased:</td>
                    <td><strong>${formatDuration(balance.purchaseAmount)}</strong></td>
                </tr>
                <tr>
                    <td>Total Used:</td>
                    <td><strong>${formatDuration(balance.totalUsedAmount)}</strong></td>
                </tr>
                <tr>
                    <td>Last Used:</td>
                    <td><strong>${formatDuration(balance.lastUsedAmount)}</strong></td>
                </tr>
                <tr class="table-primary">
                    <td><strong>Remaining:</strong></td>
                    <td><strong>${formatDuration(balance.remainAmount)}</strong></td>
                </tr>
            </table>
        `;

        new bootstrap.Modal(document.getElementById('balanceModal')).show();
    } catch (error) {
        console.error('Error loading balance:', error);
        showAlert('Error loading balance', 'danger');
    }
}

async function toggleUserStatus(userId, currentStatus) {
    const action = currentStatus ? 'disable' : 'enable';
    const endpoint = `/users/${userId}/${action}`;

    try {
        const response = await apiRequest(endpoint, { method: 'PATCH' });
        const data = await response.json();

        if (response.ok) {
            showAlert(`User ${action}d successfully`, 'success');
            await loadUsers();
        } else {
            showAlert(data.message || `Error ${action}ing user`, 'danger');
        }
    } catch (error) {
        console.error('Error toggling user status:', error);
        showAlert('Error updating user status', 'danger');
    }
}

async function deleteUser(userId, username) {
    if (!confirm(`Are you sure you want to delete user "${username}"? This will also delete their FreeSWITCH configuration.`)) {
        return;
    }

    try {
        const response = await apiRequest(`/users/${userId}`, { method: 'DELETE' });
        const data = await response.json();

        if (response.ok) {
            showAlert('User deleted successfully', 'success');
            await loadUsers();
        } else {
            showAlert(data.message || 'Error deleting user', 'danger');
        }
    } catch (error) {
        console.error('Error deleting user:', error);
        showAlert('Error deleting user', 'danger');
    }
}
