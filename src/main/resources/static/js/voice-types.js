let currentVoiceTypeId = null;

document.addEventListener('DOMContentLoaded', async function() {
    if (!requireAuth()) return;
    await loadVoiceTypes();
});

async function loadVoiceTypes() {
    try {
        const response = await apiRequest('/voice-types');
        const data = await response.json();
        const tableBody = document.getElementById('voiceTypesTable');
        
        if (!data.data || data.data.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="5" class="text-center">No voice types found</td></tr>';
            return;
        }

        tableBody.innerHTML = data.data.map(vt => `
            <tr>
                <td>${vt.id}</td>
                <td>${vt.voiceName}</td>
                <td><span class="badge bg-primary">${vt.code}</span></td>
                <td>${formatDate(vt.createdAt)}</td>
                <td>
                    <button class="btn btn-sm btn-warning" onclick="editVoiceType(${vt.id})">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-sm btn-danger" onclick="deleteVoiceType(${vt.id})">
                        <i class="bi bi-trash"></i>
                    </button>
                </td>
            </tr>
        `).join('');
    } catch (error) {
        console.error('Error loading voice types:', error);
        document.getElementById('voiceTypesTable').innerHTML = '<tr><td colspan="5" class="text-center text-danger">Error loading voice types</td></tr>';
    }
}

function openCreateVoiceTypeModal() {
    currentVoiceTypeId = null;
    document.getElementById('voiceTypeModalTitle').textContent = 'Create Voice Type';
    document.getElementById('voiceTypeForm').reset();
    document.getElementById('voiceTypeId').value = '';
}

async function editVoiceType(id) {
    try {
        const response = await apiRequest(`/voice-types/${id}`);
        const data = await response.json();
        const vt = data.data;

        currentVoiceTypeId = id;
        document.getElementById('voiceTypeModalTitle').textContent = 'Edit Voice Type';
        document.getElementById('voiceTypeId').value = id;
        document.getElementById('voiceName').value = vt.voiceName;
        document.getElementById('code').value = vt.code;

        new bootstrap.Modal(document.getElementById('voiceTypeModal')).show();
    } catch (error) {
        console.error('Error loading voice type:', error);
        showAlert('Error loading voice type', 'danger');
    }
}

async function saveVoiceType() {
    const form = document.getElementById('voiceTypeForm');
    if (!form.checkValidity()) {
        form.reportValidity();
        return;
    }

    const voiceTypeData = {
        voiceName: document.getElementById('voiceName').value,
        code: document.getElementById('code').value
    };

    try {
        const url = currentVoiceTypeId ? `/voice-types/${currentVoiceTypeId}` : '/voice-types';
        const method = currentVoiceTypeId ? 'PUT' : 'POST';

        const response = await apiRequest(url, {
            method: method,
            body: JSON.stringify(voiceTypeData)
        });

        const data = await response.json();

        if (response.ok) {
            showAlert(data.message || 'Voice type saved successfully', 'success');
            bootstrap.Modal.getInstance(document.getElementById('voiceTypeModal')).hide();
            await loadVoiceTypes();
        } else {
            showAlert(data.message || 'Error saving voice type', 'danger');
        }
    } catch (error) {
        console.error('Error saving voice type:', error);
        showAlert('Error saving voice type', 'danger');
    }
}

async function deleteVoiceType(id) {
    if (!confirm('Are you sure you want to delete this voice type?')) {
        return;
    }

    try {
        const response = await apiRequest(`/voice-types/${id}`, {
            method: 'DELETE'
        });

        const data = await response.json();

        if (response.ok) {
            showAlert(data.message || 'Voice type deleted successfully', 'success');
            await loadVoiceTypes();
        } else {
            showAlert(data.message || 'Error deleting voice type', 'danger');
        }
    } catch (error) {
        console.error('Error deleting voice type:', error);
        showAlert('Error deleting voice type', 'danger');
    }
}
