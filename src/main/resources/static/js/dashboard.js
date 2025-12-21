document.addEventListener('DOMContentLoaded', async function() {
    if (!requireAuth()) return;
    await loadDashboardStats();
    await loadRecentPurchases();
});

async function loadDashboardStats() {
    try {
        const [users, packages, purchases, voiceTypes] = await Promise.all([
            apiRequest('/users'),
            apiRequest('/packages'),
            apiRequest('/purchases'),
            apiRequest('/voice-types')
        ]);
        const usersData = await users.json();
        const packagesData = await packages.json();
        const purchasesData = await purchases.json();
        const voiceTypesData = await voiceTypes.json();
        document.getElementById('totalUsers').textContent = usersData.data?.length || 0;
        document.getElementById('totalPackages').textContent = packagesData.data?.length || 0;
        document.getElementById('totalPurchases').textContent = purchasesData.data?.length || 0;
        document.getElementById('totalVoiceTypes').textContent = voiceTypesData.data?.length || 0;
    } catch (error) {
        console.error('Error loading stats:', error);
    }
}

async function loadRecentPurchases() {
    try {
        const response = await apiRequest('/purchases');
        const data = await response.json();
        const tableBody = document.getElementById('recentPurchasesTable');
        if (!data.data || data.data.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="6" class="text-center">No purchases yet</td></tr>';
            return;
        }
        const recentPurchases = data.data.slice(0, 10);
        tableBody.innerHTML = recentPurchases.map(p => `
            <tr>
                <td>${p.id}</td>
                <td>${p.username}</td>
                <td>${p.packageName}</td>
                <td>$${formatCurrency(p.purchaseAmount)}</td>
                <td>${formatDate(p.purchaseDate)}</td>
                <td><span class="badge status-${p.transactionStatus.toLowerCase()}">${p.transactionStatus}</span></td>
            </tr>
        `).join('');
    } catch (error) {
        console.error('Error loading purchases:', error);
        document.getElementById('recentPurchasesTable').innerHTML = '<tr><td colspan="6" class="text-center text-danger">Error loading purchases</td></tr>';
    }
}
