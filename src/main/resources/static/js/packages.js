let allVoiceTypes = [];
let currentPackageId = null;

document.addEventListener('DOMContentLoaded', async function() {
    if (!requireAuth()) return;
    await loadVoiceTypes();
    await loadPackages();
});

async function loadVoiceTypes() {
    try {
        const response = await apiRequest('/voice-types');
        const data = await response.json();
        allVoiceTypes = data.data || [];
    } catch (error) {
        console.error('Error loading voice types:', error);
    }
}

async function loadPackages() {
    try {
        const response = await apiRequest('/packages');
        const data = await response.json();
        const tableBody = document.getElementById('packagesTable');
        
        if (!data.data || data.data.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="9" class="text-center">No packages found</td></tr>';
            return;
        }

        tableBody.innerHTML = data.data.map(pkg => {
            const voiceTypeNames = pkg.voiceTypes.map(vt => vt.voiceName).join(', ');
            return `
                <tr>
                    <td>${pkg.id}</td>
                    <td>${pkg.packageName}</td>
                    <td>${formatDuration(pkg.duration)}</td>
                    <td>${formatCurrency(pkg.price)}</td>
                    <td>${formatCurrency(pkg.vat)}</td>
                    <td>${formatCurrency(pkg.totalAmount)}</td>
                    <td>${voiceTypeNames}</td>
                    <td>${formatDateOnly(pkg.expireDate)}</td>
                    <td>
                        <button class="btn btn-sm btn-warning" onclick="editPackage(${pkg.id})">
                            <i class="bi bi-pencil"></i>
                        </button>
                        <button class="btn btn-sm btn-danger" onclick="deletePackage(${pkg.id})">
                            <i class="bi bi-trash"></i>
                        </button>
                    </td>
                </tr>
            `;
        }).join('');
    } catch (error) {
        console.error('Error loading packages:', error);
        document.getElementById('packagesTable').innerHTML = '<tr><td colspan="9" class="text-center text-danger">Error loading packages</td></tr>';
    }
}

function openCreatePackageModal() {
    currentPackageId = null;
    document.getElementById('packageModalTitle').textContent = 'Create Package';
    document.getElementById('packageForm').reset();
    document.getElementById('packageId').value = '';
    
    const checkboxContainer = document.getElementById('voiceTypesCheckboxes');
    checkboxContainer.innerHTML = allVoiceTypes.map(vt => `
        <div class="form-check">
            <input class="form-check-input" type="checkbox" value="${vt.id}" id="vt_${vt.id}">
            <label class="form-check-label" for="vt_${vt.id}">${vt.voiceName} (${vt.code})</label>
        </div>
    `).join('');
}

async function editPackage(id) {
    try {
        const response = await apiRequest(`/packages/${id}`);
        const data = await response.json();
        const pkg = data.data;

        currentPackageId = id;
        document.getElementById('packageModalTitle').textContent = 'Edit Package';
        document.getElementById('packageId').value = id;
        document.getElementById('packageName').value = pkg.packageName;
        document.getElementById('duration').value = pkg.duration;
        document.getElementById('price').value = pkg.price;
        document.getElementById('vat').value = pkg.vat;
        document.getElementById('expireDate').value = pkg.expireDate;

        const checkboxContainer = document.getElementById('voiceTypesCheckboxes');
        checkboxContainer.innerHTML = allVoiceTypes.map(vt => {
            const isChecked = pkg.voiceTypes.some(pvt => pvt.id === vt.id);
            return `
                <div class="form-check">
                    <input class="form-check-input" type="checkbox" value="${vt.id}" id="vt_${vt.id}" ${isChecked ? 'checked' : ''}>
                    <label class="form-check-label" for="vt_${vt.id}">${vt.voiceName} (${vt.code})</label>
                </div>
            `;
        }).join('');

        new bootstrap.Modal(document.getElementById('packageModal')).show();
    } catch (error) {
        console.error('Error loading package:', error);
        showAlert('Error loading package', 'danger');
    }
}

async function savePackage() {
    const form = document.getElementById('packageForm');
    if (!form.checkValidity()) {
        form.reportValidity();
        return;
    }

    const selectedVoiceTypes = Array.from(document.querySelectorAll('#voiceTypesCheckboxes input:checked')).map(cb => parseInt(cb.value));
    
    if (selectedVoiceTypes.length === 0) {
        showAlert('Please select at least one voice type', 'danger');
        return;
    }

    const packageData = {
        packageName: document.getElementById('packageName').value,
        duration: parseInt(document.getElementById('duration').value),
        voiceTypeIds: selectedVoiceTypes,
        expireDate: document.getElementById('expireDate').value,
        price: parseFloat(document.getElementById('price').value),
        vat: parseFloat(document.getElementById('vat').value)
    };

    try {
        const url = currentPackageId ? `/packages/${currentPackageId}` : '/packages';
        const method = currentPackageId ? 'PUT' : 'POST';

        const response = await apiRequest(url, {
            method: method,
            body: JSON.stringify(packageData)
        });

        const data = await response.json();

        if (response.ok) {
            showAlert(data.message || 'Package saved successfully', 'success');
            bootstrap.Modal.getInstance(document.getElementById('packageModal')).hide();
            await loadPackages();
        } else {
            showAlert(data.message || 'Error saving package', 'danger');
        }
    } catch (error) {
        console.error('Error saving package:', error);
        showAlert('Error saving package', 'danger');
    }
}

async function deletePackage(id) {
    if (!confirm('Are you sure you want to delete this package?')) {
        return;
    }

    try {
        const response = await apiRequest(`/packages/${id}`, {
            method: 'DELETE'
        });

        const data = await response.json();

        if (response.ok) {
            showAlert(data.message || 'Package deleted successfully', 'success');
            await loadPackages();
        } else {
            showAlert(data.message || 'Error deleting package', 'danger');
        }
    } catch (error) {
        console.error('Error deleting package:', error);
        showAlert('Error deleting package', 'danger');
    }
}
