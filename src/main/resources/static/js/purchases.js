document.addEventListener('DOMContentLoaded', async function() {
    if (!requireAuth()) return;
    await loadPurchases();
});

async function loadPurchases() {
    try {
        const response = await apiRequest('/purchases');
        const data = await response.json();
        const tableBody = document.getElementById('purchasesTable');
        
        if (!data.data || data.data.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="8" class="text-center">No purchases found</td></tr>';
            return;
        }

        tableBody.innerHTML = data.data.map(p => {
            const statusClass = p.transactionStatus === 'SUCCESS' ? 'success' : 
                               p.transactionStatus === 'PENDING' ? 'warning' : 'danger';
            return `
                <tr>
                    <td>${p.id}</td>
                    <td>${p.username}</td>
                    <td>${p.packageName}</td>
                    <td>${formatDuration(p.duration)}</td>
                    <td>${formatCurrency(p.purchaseAmount)}</td>
                    <td>${p.transactionId}</td>
                    <td><span class="badge bg-${statusClass}">${p.transactionStatus}</span></td>
                    <td>${formatDate(p.purchaseDate)}</td>
                </tr>
            `;
        }).join('');
    } catch (error) {
        console.error('Error loading purchases:', error);
        document.getElementById('purchasesTable').innerHTML = '<tr><td colspan="8" class="text-center text-danger">Error loading purchases</td></tr>';
    }
}
