/**
 * HTTP API client for the Library Management System REST backend.
 * Handles authentication headers, JSON serialization, and error responses.
 */
const API = (() => {
    const BASE = '/api';

    /** Sends an HTTP request with auth headers and JSON body. */
    async function request(method, path, body = null) {
        const headers = { 'Content-Type': 'application/json' };
        const token = Auth.getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const opts = { method, headers };
        if (body) {
            opts.body = JSON.stringify(body);
        }

        const res = await fetch(`${BASE}${path}`, opts);

        if (res.status === 401) {
            Auth.clearSession();
            App.navigate('login');
            throw new Error('Session expired. Please log in again.');
        }

        const data = await res.json().catch(() => ({}));

        if (!res.ok) {
            throw new Error(data.error || `Request failed (${res.status})`);
        }

        return data;
    }

    return {
        get:    (path)       => request('GET', path),
        post:   (path, body) => request('POST', path, body),
        put:    (path, body) => request('PUT', path, body),
        delete: (path)       => request('DELETE', path),
    };
})();
