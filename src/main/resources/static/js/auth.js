/**
 * Authentication module — manages login state, token storage,
 * and session persistence across page reloads.
 */
const Auth = (() => {
    const STORAGE_KEY = 'lms_session';

    function saveSession(data) {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
    }

    function getSession() {
        try {
            return JSON.parse(localStorage.getItem(STORAGE_KEY));
        } catch { return null; }
    }

    function clearSession() {
        localStorage.removeItem(STORAGE_KEY);
    }

    function getToken() {
        const s = getSession();
        return s ? s.token : null;
    }

    function getRole() {
        const s = getSession();
        return s ? s.role : null;
    }

    function getUsername() {
        const s = getSession();
        return s ? s.username : null;
    }

    function isLoggedIn() {
        return !!getToken();
    }

    async function login(username, password) {
        const data = await API.post('/login', { username, password });
        saveSession(data);
        return data;
    }

    async function logout() {
        try { await API.post('/logout'); } catch {}
        clearSession();
    }

    async function changePassword(oldPassword, newPassword) {
        return API.post('/change-password', { oldPassword, newPassword });
    }

    return {
        saveSession, getSession, clearSession,
        getToken, getRole, getUsername, isLoggedIn,
        login, logout, changePassword
    };
})();
