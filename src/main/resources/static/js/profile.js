/**
 * Profile Management — JavaScript
 * Handles: Load profile, update profile, upload avatar, change password
 */
document.addEventListener('DOMContentLoaded', () => {
    loadProfile();
    initEventListeners();
});

// ─── State ───────────────────────────────────────────────────────────
let profileData = null;

// ─── Load Profile ────────────────────────────────────────────────────
async function loadProfile() {
    try {
        const res = await fetch('/api/profile', {
            headers: { 'Accept': 'application/json' }
        });
        const json = await res.json();

        if (json.success && json.data) {
            profileData = json.data;
            populateForm(profileData);
        }
    } catch (err) {
        console.error('Failed to load profile:', err);
    }
}

function populateForm(data) {
    // Display info
    const displayName = document.getElementById('display-name');
    const displayEmail = document.getElementById('display-email');
    const displayRole = document.getElementById('display-role');
    const avatarPreview = document.getElementById('avatar-preview');
    const avatarPlaceholder = document.getElementById('avatar-placeholder');
    const avatarInitial = document.getElementById('avatar-initial');

    if (displayName) displayName.textContent = data.fullName || '';
    if (displayEmail) displayEmail.textContent = data.email || '';
    if (displayRole) displayRole.textContent = data.role || '';

    // Avatar
    if (data.avatarPath) {
        if (avatarPreview) {
            avatarPreview.src = data.avatarPath;
            avatarPreview.classList.remove('hidden');
        }
        if (avatarPlaceholder) avatarPlaceholder.classList.add('hidden');
    } else {
        if (avatarPreview) avatarPreview.classList.add('hidden');
        if (avatarPlaceholder) avatarPlaceholder.classList.remove('hidden');
        if (avatarInitial && data.fullName) {
            avatarInitial.textContent = data.fullName.charAt(0).toUpperCase();
        }
    }

    // Form fields
    const fullNameInput = document.getElementById('fullName');
    const phoneInput = document.getElementById('phone');
    const emailInput = document.getElementById('email');

    if (fullNameInput) fullNameInput.value = data.fullName || '';
    if (phoneInput) phoneInput.value = data.phone || '';
    if (emailInput) emailInput.value = data.email || '';

    // Owner section
    if (data.role === 'OWNER') {
        const ownerSection = document.getElementById('owner-section');
        if (ownerSection) {
            ownerSection.classList.remove('hidden');
            const businessName = document.getElementById('businessName');
            const taxCode = document.getElementById('taxCode');
            const bankName = document.getElementById('bankName');
            const bankAccountNo = document.getElementById('bankAccountNo');
            const bankAccountName = document.getElementById('bankAccountName');

            if (businessName) businessName.value = data.businessName || '';
            if (taxCode) taxCode.value = data.taxCode || '';
            if (bankName) bankName.value = data.bankName || '';
            if (bankAccountNo) bankAccountNo.value = data.bankAccountNo || '';
            if (bankAccountName) bankAccountName.value = data.bankAccountName || '';
        }
    }

    // Hide password section for OAuth users
    if (data.oauthUser) {
        const passwordSection = document.getElementById('password-section');
        if (passwordSection) passwordSection.style.display = 'none';
    }
}

// ─── Event Listeners ─────────────────────────────────────────────────
function initEventListeners() {
    // Avatar file input
    const avatarInput = document.getElementById('avatar-input');
    if (avatarInput) {
        avatarInput.addEventListener('change', handleAvatarUpload);
    }

    // Profile form
    const profileForm = document.getElementById('profile-form');
    if (profileForm) {
        profileForm.addEventListener('submit', handleProfileSubmit);
    }

    // Owner form
    const ownerForm = document.getElementById('owner-form');
    if (ownerForm) {
        ownerForm.addEventListener('submit', handleOwnerSubmit);
    }

    // Password toggle
    const togglePassword = document.getElementById('toggle-password');
    if (togglePassword) {
        togglePassword.addEventListener('click', togglePasswordSection);
    }

    // Password form
    const passwordForm = document.getElementById('password-form');
    if (passwordForm) {
        passwordForm.addEventListener('submit', handlePasswordSubmit);
    }
}

// ─── Avatar Upload ───────────────────────────────────────────────────
async function handleAvatarUpload(e) {
    const file = e.target.files[0];
    if (!file) return;

    // Client-side validation
    const maxSize = 2 * 1024 * 1024; // 2MB
    if (file.size > maxSize) {
        showToast('File size must be less than 2MB', 'error');
        return;
    }

    const allowedTypes = ['image/jpeg', 'image/png', 'image/webp'];
    if (!allowedTypes.includes(file.type)) {
        showToast('Only JPG, PNG, and WebP images are allowed', 'error');
        return;
    }

    // Preview immediately
    const reader = new FileReader();
    reader.onload = (evt) => {
        const preview = document.getElementById('avatar-preview');
        const placeholder = document.getElementById('avatar-placeholder');
        if (preview) {
            preview.src = evt.target.result;
            preview.classList.remove('hidden');
        }
        if (placeholder) placeholder.classList.add('hidden');
    };
    reader.readAsDataURL(file);

    // Upload
    const uploadProgress = document.getElementById('upload-progress');
    if (uploadProgress) uploadProgress.classList.remove('hidden');

    try {
        const formData = new FormData();
        formData.append('file', file);

        const res = await fetch('/api/profile/avatar', {
            method: 'POST',
            body: formData
        });
        const json = await res.json();

        if (json.success) {
            showToast('Avatar updated successfully!', 'success');
            // Update preview with Cloudinary URL
            const preview = document.getElementById('avatar-preview');
            if (preview && json.data) {
                preview.src = json.data;
            }
            
            // Update navbar avatars
            const navAvatarImgs = document.querySelectorAll('.nav-avatar-img');
            const navAvatarPlaceholders = document.querySelectorAll('.nav-avatar-placeholder');
            
            navAvatarImgs.forEach(img => {
                img.src = json.data;
                img.classList.remove('hidden');
            });
            
            navAvatarPlaceholders.forEach(placeholder => {
                placeholder.classList.add('hidden');
            });
        } else {
            showToast(json.message || 'Failed to upload avatar', 'error');
        }
    } catch (err) {
        console.error('Avatar upload failed:', err);
        showToast('Failed to upload avatar', 'error');
    } finally {
        if (uploadProgress) uploadProgress.classList.add('hidden');
        // Reset input so same file can be re-selected
        e.target.value = '';
    }
}

// ─── Profile Submit ──────────────────────────────────────────────────
async function handleProfileSubmit(e) {
    e.preventDefault();
    const btn = document.getElementById('btn-save-profile');
    setButtonLoading(btn, true);

    try {
        const res = await fetch('/api/profile', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                fullName: document.getElementById('fullName').value,
                phone: document.getElementById('phone').value
            })
        });
        const json = await res.json();

        if (json.success) {
            showToast('Profile updated successfully!', 'success');
            // Update display name
            const displayName = document.getElementById('display-name');
            if (displayName) displayName.textContent = document.getElementById('fullName').value;
        } else {
            showToast(json.message || 'Failed to update profile', 'error');
        }
    } catch (err) {
        console.error('Profile update failed:', err);
        showToast('Failed to update profile', 'error');
    } finally {
        setButtonLoading(btn, false);
    }
}

// ─── Owner Submit ────────────────────────────────────────────────────
async function handleOwnerSubmit(e) {
    e.preventDefault();
    const btn = document.getElementById('btn-save-owner');
    setButtonLoading(btn, true);

    try {
        const res = await fetch('/api/profile/owner', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                fullName: document.getElementById('fullName').value,
                phone: document.getElementById('phone').value,
                businessName: document.getElementById('businessName').value,
                taxCode: document.getElementById('taxCode').value,
                bankName: document.getElementById('bankName').value,
                bankAccountNo: document.getElementById('bankAccountNo').value,
                bankAccountName: document.getElementById('bankAccountName').value
            })
        });
        const json = await res.json();

        if (json.success) {
            showToast('Business info updated successfully!', 'success');
        } else {
            showToast(json.message || 'Failed to update business info', 'error');
        }
    } catch (err) {
        console.error('Owner profile update failed:', err);
        showToast('Failed to update business info', 'error');
    } finally {
        setButtonLoading(btn, false);
    }
}

// ─── Password ────────────────────────────────────────────────────────
function togglePasswordSection() {
    const container = document.getElementById('password-form-container');
    const chevron = document.getElementById('chevron-icon');

    if (container) {
        container.classList.toggle('hidden');
    }
    if (chevron) {
        chevron.classList.toggle('rotate-180');
    }
}

async function handlePasswordSubmit(e) {
    e.preventDefault();
    const btn = document.getElementById('btn-change-password');
    
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    if (newPassword !== confirmPassword) {
        showToast('New passwords do not match', 'error');
        return;
    }

    setButtonLoading(btn, true);

    try {
        const res = await fetch('/api/profile/password', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                currentPassword: document.getElementById('currentPassword').value,
                newPassword: newPassword,
                confirmPassword: confirmPassword
            })
        });
        const json = await res.json();

        if (json.success) {
            showToast('Password changed successfully!', 'success');
            document.getElementById('password-form').reset();
            togglePasswordSection();
        } else {
            showToast(json.message || 'Failed to change password', 'error');
        }
    } catch (err) {
        console.error('Password change failed:', err);
        showToast('Failed to change password', 'error');
    } finally {
        setButtonLoading(btn, false);
    }
}

// ─── UI Helpers ──────────────────────────────────────────────────────
function setButtonLoading(btn, loading) {
    if (!btn) return;
    if (loading) {
        btn.disabled = true;
        btn.dataset.originalText = btn.innerHTML;
        btn.innerHTML = `
            <svg class="animate-spin w-4 h-4" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            <span>Processing...</span>`;
        btn.classList.add('opacity-75', 'cursor-not-allowed');
    } else {
        btn.disabled = false;
        btn.innerHTML = btn.dataset.originalText;
        btn.classList.remove('opacity-75', 'cursor-not-allowed');
    }
}

function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    const toastIcon = document.getElementById('toast-icon');
    const toastMessage = document.getElementById('toast-message');

    if (!toast || !toastMessage) return;

    const container = toast.querySelector('div');

    // Reset classes
    container.className = 'flex items-center gap-3 px-5 py-4 rounded-xl shadow-lg border max-w-sm';

    if (type === 'success') {
        container.classList.add('bg-emerald-50', 'border-emerald-200', 'text-emerald-800');
        if (toastIcon) toastIcon.innerHTML = '<svg class="w-5 h-5 text-emerald-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>';
    } else {
        container.classList.add('bg-rose-50', 'border-rose-200', 'text-rose-800');
        if (toastIcon) toastIcon.innerHTML = '<svg class="w-5 h-5 text-rose-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>';
    }

    toastMessage.textContent = message;

    // Show
    toast.classList.remove('hidden');
    requestAnimationFrame(() => {
        toast.classList.remove('translate-y-2', 'opacity-0');
        toast.classList.add('translate-y-0', 'opacity-100');
    });

    // Auto-hide after 4s
    setTimeout(() => {
        toast.classList.remove('translate-y-0', 'opacity-100');
        toast.classList.add('translate-y-2', 'opacity-0');
        setTimeout(() => toast.classList.add('hidden'), 300);
    }, 4000);
}
