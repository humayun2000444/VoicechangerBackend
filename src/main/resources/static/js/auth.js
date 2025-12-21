document.addEventListener('DOMContentLoaded', function() {
    // Redirect if already authenticated
    if (isAuthenticated()) {
        window.location.href = '/admin/dashboard.html';
        return;
    }

    const loginForm = document.getElementById('loginForm');
    const loginBtn = document.getElementById('loginBtn');
    const loginBtnText = document.getElementById('loginBtnText');
    const loginSpinner = document.getElementById('loginSpinner');

    loginForm.addEventListener('submit', async function(e) {
        e.preventDefault();

        const username = document.getElementById('username').value;
        const password = document.getElementById('password').value;

        // Disable button
        loginBtn.disabled = true;
        loginBtnText.classList.add('d-none');
        loginSpinner.classList.remove('d-none');

        try {
            const response = await fetch(`${API_BASE_URL}/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ username, password })
            });

            if (response.ok) {
                const data = await response.json();

                // Check if user has ROLE_ADMIN
                if (data.roles && data.roles.includes('ROLE_ADMIN')) {
                    setToken(data.token);
                    localStorage.setItem('username', data.username);
                    showAlert('Login successful! Redirecting...', 'success');
                    setTimeout(() => {
                        window.location.href = '/admin/dashboard.html';
                    }, 1000);
                } else {
                    showAlert('Access denied. Admin privileges required.', 'danger');
                    loginBtn.disabled = false;
                    loginBtnText.classList.remove('d-none');
                    loginSpinner.classList.add('d-none');
                }
            } else {
                const data = await response.json();
                showAlert(data.message || 'Login failed. Please check your credentials.', 'danger');
                loginBtn.disabled = false;
                loginBtnText.classList.remove('d-none');
                loginSpinner.classList.add('d-none');
            }
        } catch (error) {
            console.error('Login error:', error);
            showAlert('Connection error. Please check if the server is running.', 'danger');
            loginBtn.disabled = false;
            loginBtnText.classList.remove('d-none');
            loginSpinner.classList.add('d-none');
        }
    });
});
