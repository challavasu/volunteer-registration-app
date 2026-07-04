/**
 * Login Form Handler
 */

async function handleLogin(e) {
    e.preventDefault();

    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;

    console.log('🔐 Login attempt - Username:', username);

    if (!username || !password) {
        showError('Please enter username and password');
        return;
    }

    const submitBtn = document.querySelector('.submit-btn');
    submitBtn.disabled = true;
    submitBtn.textContent = 'Signing in...';

    try {
        const csrfElement = document.getElementById('csrfToken');
        if (!csrfElement) {
            console.error('CSRF token element not found');
            showError('Security error: CSRF token missing');
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<i class="fas fa-sign-in-alt"></i> Sign In';
            return;
        }

        const csrfToken = csrfElement.value || csrfElement.getAttribute('value');
        const csrfParamName = csrfElement.getAttribute('name') || '_csrf';

        console.log('🔐 CSRF Token extracted:', csrfToken ? 'yes' : 'NO');
        console.log('🔐 CSRF Param Name:', csrfParamName);

        // Build form data as URL encoded
        const params = new URLSearchParams();
        params.append('username', username);
        params.append('password', password);
        params.append(csrfParamName, csrfToken);

        console.log('🔐 Sending to /api/auth/login...');

        const response = await fetch('/api/auth/login', {
            method: 'POST',
            body: params,
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-CSRF-TOKEN': csrfToken
            }
        });

        console.log('Response status:', response.status);
        console.log('Response headers:', response.headers);

        const responseText = await response.text();
        console.log('Raw response:', responseText);

        let data;
        try {
            data = JSON.parse(responseText);
        } catch (e) {
            console.error('Failed to parse JSON:', responseText);
            showError('Server error: Invalid response');
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<i class="fas fa-sign-in-alt"></i> Sign In';
            return;
        }

        console.log('Parsed data:', data);

        if (data.success === true) {
            console.log('✅ Login successful! Redirecting...');
            // Wait a moment then redirect
            setTimeout(() => {
                window.location.href = '/admin';
            }, 500);
        } else {
            console.log('❌ Login failed. Success:', data.success, 'Message:', data.message);
            showError(data.message || 'Invalid username or password');
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<i class="fas fa-sign-in-alt"></i> Sign In';
        }
    } catch (error) {
        console.error('❌ Error:', error);
        showError('Error: ' + error.message);
        submitBtn.disabled = false;
        submitBtn.innerHTML = '<i class="fas fa-sign-in-alt"></i> Sign In';
    }
}

function showError(message) {
    const errorDiv = document.getElementById('errorMessage');
    const errorText = document.getElementById('errorText');
    errorText.textContent = message;
    errorDiv.classList.add('show');
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('loginForm');
    if (form) {
        form.addEventListener('submit', handleLogin);
    }
});
