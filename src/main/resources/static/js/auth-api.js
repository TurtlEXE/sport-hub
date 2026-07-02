// auth-api.js - Handles REST API fetching for authentication forms

/**
 * Handle form submission via fetch API
 * @param {HTMLFormElement} formElement - The form to submit
 * @param {string} endpoint - The API endpoint to call
 * @param {function} onSuccess - Callback when response.success === true
 */
function handleAuthFormSubmit(formElement, endpoint, onSuccess) {
    if (!formElement) return;

    formElement.addEventListener('submit', async (e) => {
        e.preventDefault();

        const submitBtn = formElement.querySelector('button[type="submit"]');
        let alertBox = formElement.querySelector('.auth-alert');
        if (!alertBox && formElement.parentElement) {
            alertBox = formElement.parentElement.querySelector('.auth-alert');
        }
        
        // Disable button & show spinner
        if (submitBtn) {
            submitBtn.disabled = true;
            const originalText = submitBtn.innerHTML;
            submitBtn.setAttribute('data-original', originalText);
            submitBtn.innerHTML = `<span class="animate-spin h-4 w-4 border-2 border-white border-t-transparent rounded-full inline-block mr-2"></span> Processing...`;
        }

        // Hide alert box if visible
        if (alertBox) {
            alertBox.classList.add('hidden');
            alertBox.classList.remove('bg-rose-50', 'text-rose-800', 'border-rose-100', 'bg-emerald-50', 'text-emerald-800', 'border-emerald-100');
        }

        try {
            // Collect form data
            const formData = new FormData(formElement);
            const data = Object.fromEntries(formData.entries());

            // Add checkboxes manually if they are unchecked (FormData ignores them)
            formElement.querySelectorAll('input[type="checkbox"]').forEach(checkbox => {
                if (!checkbox.checked && checkbox.name) {
                    data[checkbox.name] = false;
                } else if (checkbox.checked && checkbox.name) {
                    data[checkbox.name] = true;
                }
            });

            const response = await fetch(endpoint, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(data)
            });

            let result = {};
            // Attempt to parse JSON response
            const text = await response.text();
            if (text) {
                try {
                    result = JSON.parse(text);
                } catch(e) {
                    console.error("JSON parsing error", e);
                }
            }

            if (response.ok) {
                // Check if result has our generic ApiResponse format
                if (result.success !== false) {
                    if (onSuccess) {
                        onSuccess(result, data);
                    }
                } else {
                    showAuthAlert(alertBox, result.message || 'Action failed', 'error');
                }
            } else {
                // E.g. validation errors or 401/403
                let errorMsg = 'An error occurred. Please try again.';
                if (result.errors && Object.keys(result.errors).length > 0) {
                    const firstKey = Object.keys(result.errors)[0];
                    errorMsg = result.errors[firstKey];
                } else if (result.message) {
                    errorMsg = result.message;
                } else if (response.status === 401 || response.status === 403) {
                    errorMsg = 'Invalid credentials or unauthorized access.';
                }
                showAuthAlert(alertBox, errorMsg, 'error');
            }
        } catch (error) {
            console.error('Fetch error:', error);
            showAuthAlert(alertBox, 'Network error. Please check your connection.', 'error');
        } finally {
            // Restore button
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.innerHTML = submitBtn.getAttribute('data-original');
            }
        }
    });
}

function showAuthAlert(alertElement, message, type) {
    if (!alertElement) return;
    
    alertElement.classList.remove('hidden');
    const iconSpan = alertElement.querySelector('.alert-icon');
    const msgSpan = alertElement.querySelector('.alert-msg');
    
    if (msgSpan) msgSpan.textContent = message;

    if (type === 'error') {
        alertElement.classList.add('bg-rose-50', 'text-rose-800', 'border-rose-100');
        if (iconSpan) iconSpan.innerHTML = `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-rose-500"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>`;
    } else {
        alertElement.classList.add('bg-emerald-50', 'text-emerald-800', 'border-emerald-100');
        if (iconSpan) iconSpan.innerHTML = `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-emerald-500"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>`;
    }
}
