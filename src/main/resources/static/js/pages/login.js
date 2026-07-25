/**
 * Login page — full-screen authentication form with role selector.
 */
const LoginPage = (() => {
    function render(container) {
        container.innerHTML = `
            <div class="login-wrapper">
                <div class="login-card">
                    <img src="/assets/logo.png" alt="Library" class="login-logo">
                    <h1 class="login-title">Central Library</h1>
                    <p class="login-subtitle">Sign in to your account</p>

                    <div id="login-error" class="hidden" style="
                        background: var(--red-bg);
                        border: 1px solid rgba(248,113,113,0.2);
                        border-radius: var(--radius-sm);
                        padding: 10px 14px;
                        margin-bottom: 16px;
                        font-size: 0.8125rem;
                        color: var(--red);
                    "></div>

                    <div class="form-group">
                        <label class="form-label" for="login-role">Role</label>
                        <select class="form-select" id="login-role">
                            <option value="ADMIN">Administrator</option>
                            <option value="LIBRARIAN">Librarian</option>
                            <option value="STUDENT">Student</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="login-user">Username</label>
                        <input class="form-input" id="login-user" type="text"
                               placeholder="Enter your username" autocomplete="username">
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="login-pass">Password</label>
                        <input class="form-input" id="login-pass" type="password"
                               placeholder="Enter your password" autocomplete="current-password">
                    </div>

                    <button class="btn btn-primary w-full mt-md" id="login-btn" style="height:44px;">
                        Sign In
                    </button>

                    <p style="text-align:center; margin-top:16px; font-size:0.8125rem; color:var(--text-muted);">
                        Contact your administrator for login credentials
                    </p>
                    <p class="login-footer">Version 2.0.0 &bull; Enterprise Edition</p>
                </div>
            </div>
        `;

        const btn  = document.getElementById('login-btn');
        const user = document.getElementById('login-user');
        const pass = document.getElementById('login-pass');
        const err  = document.getElementById('login-error');

        async function handleLogin() {
            const username = user.value.trim();
            const password = pass.value;
            if (!username || !password) {
                showError('Please enter both username and password.');
                return;
            }
            btn.disabled = true;
            btn.textContent = 'Signing in...';
            try {
                await Auth.login(username, password);
                err.classList.add('hidden');
                App.navigate('dashboard');
            } catch (e) {
                showError(e.message);
                pass.value = '';
                pass.focus();
            } finally {
                btn.disabled = false;
                btn.textContent = 'Sign In';
            }
        }

        function showError(msg) {
            err.textContent = msg;
            err.classList.remove('hidden');
        }

        btn.addEventListener('click', handleLogin);
        pass.addEventListener('keydown', (e) => { if (e.key === 'Enter') handleLogin(); });
        user.addEventListener('keydown', (e) => { if (e.key === 'Enter') pass.focus(); });
        user.focus();
    }

    return { render };
})();
