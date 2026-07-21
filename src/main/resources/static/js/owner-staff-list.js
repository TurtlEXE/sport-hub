document.addEventListener('DOMContentLoaded', function () {
    loadStaffList();
    loadFacilities();

    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('input', function () {
            filterStaff(this.value.toLowerCase());
        });
    }

    document.getElementById('staffForm').addEventListener('submit', function(e) {
        e.preventDefault();
        submitStaffForm();
    });
});

let allStaff = [];

function loadStaffList() {
    fetch('/api/owner/staff')
        .then(res => res.json())
        .then(response => {
            if (response.success) {
                allStaff = response.data || [];
                renderStaffList(allStaff);
                updateStats(allStaff);
            }
        })
        .catch(err => console.error('Error loading staff:', err));
}

function renderStaffList(staffList) {
    const tbody = document.getElementById('staffTableBody');
    const emptyState = document.getElementById('emptyState');
    const tableContainer = document.getElementById('staffTableContainer');

    if (staffList.length === 0) {
        emptyState.classList.remove('hidden');
        tableContainer.classList.add('hidden');
        return;
    }

    emptyState.classList.add('hidden');
    tableContainer.classList.remove('hidden');

    tbody.innerHTML = staffList.map(staff => `
        <tr class="hover:bg-slate-50 transition-colors">
            <td class="py-4 px-6">
                <div class="flex items-center gap-3">
                    <div class="w-9 h-9 bg-blue-100 text-blue-600 rounded-full flex items-center justify-center font-bold text-sm overflow-hidden flex-shrink-0">
                        ${staff.avatarPath
                            ? `<img src="${staff.avatarPath}" class="w-full h-full object-cover" alt="">`
                            : `<span>${staff.fullName.charAt(0).toUpperCase()}</span>`
                        }
                    </div>
                    <div class="min-w-0">
                        <div class="font-semibold text-slate-900 text-sm truncate">${staff.fullName}</div>
                        <div class="text-[10px] text-slate-400">ID: ${staff.staffId}</div>
                    </div>
                </div>
            </td>
            <td class="py-4 px-6 text-sm text-slate-600">${staff.email}</td>
            <td class="py-4 px-6 text-sm text-slate-600 hidden md:table-cell">${staff.phone || '-'}</td>
            <td class="py-4 px-6">
                ${staff.facilityName 
                    ? `<span class="inline-flex items-center gap-1.5 px-2.5 py-1 bg-indigo-50 text-indigo-700 text-xs font-semibold rounded-lg">
                        <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"></path></svg>
                        ${staff.facilityName}
                       </span>`
                    : `<span class="inline-flex items-center gap-1.5 px-2.5 py-1 bg-slate-50 text-slate-500 text-xs font-semibold rounded-lg">
                        ${i18nStaff.notAssigned}
                       </span>`
                }
            </td>

            <td class="py-4 px-6 text-center">
                <div class="flex items-center justify-center gap-2">
                    <button type="button" onclick="openStaffModal(${staff.staffId})"
                        class="p-2 text-blue-500 hover:text-blue-700 hover:bg-blue-50 rounded-lg transition-colors"
                        title="Edit">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path></svg>
                    </button>
                    <button type="button" onclick="deleteStaff(${staff.staffId})"
                        class="p-2 text-rose-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors"
                        title="Delete">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

function updateStats(staffList) {
    const total = staffList.length;
    const active = staffList.filter(s => s.isActive).length;
    const inactive = total - active;

    document.getElementById('statTotal').textContent = total;
    document.getElementById('statActive').textContent = active;
    document.getElementById('statInactive').textContent = inactive;
}

function filterStaff(query) {
    if (!query) {
        renderStaffList(allStaff);
        return;
    }
    const filtered = allStaff.filter(s =>
        s.fullName.toLowerCase().includes(query) ||
        s.email.toLowerCase().includes(query)
    );
    renderStaffList(filtered);
}

function deleteStaff(staffId) {
    if (typeof showCustomConfirm === 'function') {
        showCustomConfirm(i18nStaff.confirmDelete, () => {
            performDelete(staffId);
        }, 'danger');
    } else if (confirm(i18nStaff.confirmDelete)) {
        performDelete(staffId);
    }
}

function performDelete(staffId) {
    fetch(`/api/owner/staff/${staffId}`, { method: 'DELETE' })
        .then(res => res.json())
        .then(response => {
            if (response.success) {
                if (typeof showCustomAlert === 'function') {
                    showCustomAlert(response.message, 'success', () => loadStaffList());
                } else {
                    loadStaffList();
                }
            } else {
                if (typeof showCustomAlert === 'function') {
                    showCustomAlert(response.message, 'error');
                } else {
                    alert(response.message);
                }
            }
        })
        .catch(err => console.error('Error deleting staff:', err));
}

let currentStaffId = null;

function loadFacilities() {
    fetch('/api/owner/facilities')
        .then(res => res.json())
        .then(facilities => {
            if (Array.isArray(facilities)) {
                const select = document.getElementById('facilityId');
                select.innerHTML = `<option value="">${i18nStaff.notAssigned}</option>`;
                facilities.forEach(f => {
                    const opt = document.createElement('option');
                    opt.value = f.facilityId;
                    opt.textContent = f.name;
                    select.appendChild(opt);
                });
            }
        })
        .catch(err => console.error('Error loading facilities:', err));
}

function openStaffModal(id = null) {
    currentStaffId = id;
    const isEdit = !!id;
    
    document.getElementById('formError').classList.add('hidden');
    document.getElementById('staffForm').reset();
    
    if (isEdit) {
        document.getElementById('modalTitle').textContent = i18nStaff.titleEdit;
        document.getElementById('modalSubtitle').textContent = i18nStaff.subtitleEdit;
        document.getElementById('passwordHint').classList.remove('hidden');
        
        // Fetch staff data
        fetch(`/api/owner/staff/${id}`)
            .then(res => res.json())
            .then(response => {
                if (response.success) {
                    const staff = response.data;
                    document.getElementById('fullName').value = staff.fullName || '';
                    document.getElementById('email').value = staff.email || '';
                    document.getElementById('phone').value = staff.phone || '';
                    document.getElementById('facilityId').value = staff.facilityId || '';
                }
            });
    } else {
        document.getElementById('modalTitle').textContent = i18nStaff.titleCreate;
        document.getElementById('modalSubtitle').textContent = i18nStaff.subtitleCreate;
        document.getElementById('passwordHint').classList.add('hidden');
    }
    
    const modal = document.getElementById('staffModal');
    const content = document.getElementById('staffModalContent');
    const backdrop = document.getElementById('staffModalBackdrop');
    
    modal.classList.remove('hidden');
    // Trigger reflow
    void modal.offsetWidth;
    
    backdrop.classList.remove('opacity-0');
    backdrop.classList.add('opacity-100');
    content.classList.remove('opacity-0', 'scale-95');
    content.classList.add('opacity-100', 'scale-100');
}

function closeStaffModal() {
    const modal = document.getElementById('staffModal');
    const content = document.getElementById('staffModalContent');
    const backdrop = document.getElementById('staffModalBackdrop');
    
    backdrop.classList.remove('opacity-100');
    backdrop.classList.add('opacity-0');
    content.classList.remove('opacity-100', 'scale-100');
    content.classList.add('opacity-0', 'scale-95');
    
    setTimeout(() => {
        modal.classList.add('hidden');
    }, 300);
}

function submitStaffForm() {
    const formError = document.getElementById('formError');
    const formErrorText = document.getElementById('formErrorText');
    formError.classList.add('hidden');

    const facilitySelectVal = document.getElementById('facilityId').value;
    const payload = {
        fullName: document.getElementById('fullName').value.trim(),
        email: document.getElementById('email').value.trim(),
        phone: document.getElementById('phone').value.trim() || null,
        password: document.getElementById('password').value || null,
        facilityId: facilitySelectVal ? parseInt(facilitySelectVal) : null
    };

    if (!payload.fullName || !payload.email) {
        formErrorText.textContent = 'Please fill in all required fields.';
        formError.classList.remove('hidden');
        return;
    }

    if (!currentStaffId && (!payload.password || payload.password.length < 6)) {
        formErrorText.textContent = 'Password is required and must be at least 6 characters.';
        formError.classList.remove('hidden');
        return;
    }

    const url = currentStaffId ? `/api/owner/staff/${currentStaffId}` : '/api/owner/staff';
    const method = currentStaffId ? 'PUT' : 'POST';

    const btn = document.getElementById('btnSubmit');
    btn.disabled = true;
    btn.classList.add('opacity-50');

    fetch(url, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
        .then(res => res.json())
        .then(response => {
            if (response.success) {
                if (typeof showCustomAlert === 'function') {
                    showCustomAlert(response.message, 'success', () => {
                        closeStaffModal();
                        loadStaffList();
                    });
                } else {
                    closeStaffModal();
                    loadStaffList();
                }
            } else {
                formErrorText.textContent = response.message || 'An error occurred.';
                formError.classList.remove('hidden');
            }
        })
        .catch(err => {
            console.error('Error:', err);
            formErrorText.textContent = 'Network error. Please try again.';
            formError.classList.remove('hidden');
        })
        .finally(() => {
            btn.disabled = false;
            btn.classList.remove('opacity-50');
        });
}
