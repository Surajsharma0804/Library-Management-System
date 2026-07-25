/**
 * Service Worker — caches the app shell for offline access.
 * Uses a cache-first strategy for static assets and
 * network-first for API calls.
 */
const CACHE_NAME = 'lms-v2.1.0';
const SHELL_ASSETS = [
    '/',
    '/index.html',
    '/css/app.css',
    '/js/api.js',
    '/js/auth.js',
    '/js/app.js',
    '/js/components/toast.js',
    '/js/components/modal.js',
    '/js/components/sidebar.js',
    '/js/pages/login.js',
    '/js/pages/dashboard.js',
    '/js/pages/catalog.js',
    '/js/pages/borrows.js',
    '/js/pages/fines.js',
    '/js/pages/students.js',
    '/js/pages/books-mgmt.js',
    '/js/pages/profile.js',
    '/assets/logo.png',
    '/manifest.json',
];

// Pre-cache app shell on install
self.addEventListener('install', (event) => {
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then(cache => cache.addAll(SHELL_ASSETS))
            .then(() => self.skipWaiting())
    );
});

// Clean up old caches on activate
self.addEventListener('activate', (event) => {
    event.waitUntil(
        caches.keys().then(keys =>
            Promise.all(
                keys.filter(k => k !== CACHE_NAME).map(k => caches.delete(k))
            )
        ).then(() => self.clients.claim())
    );
});

// Network-first for API, cache-first for assets
self.addEventListener('fetch', (event) => {
    const url = new URL(event.request.url);

    // API calls — always go to network
    if (url.pathname.startsWith('/api/')) {
        event.respondWith(fetch(event.request));
        return;
    }

    // Static assets — cache first, fallback to network
    event.respondWith(
        caches.match(event.request).then(cached => {
            if (cached) return cached;
            return fetch(event.request).then(response => {
                if (response.ok) {
                    const clone = response.clone();
                    caches.open(CACHE_NAME).then(cache => cache.put(event.request, clone));
                }
                return response;
            });
        }).catch(() => caches.match('/index.html'))
    );
});
