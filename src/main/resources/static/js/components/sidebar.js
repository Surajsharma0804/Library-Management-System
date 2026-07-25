/**
 * Sidebar navigation component — renders the navigation menu
 * based on the authenticated user's role.
 */
const Sidebar = (() => {
    const ICONS = {
        dashboard: `<svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></svg>`,
        books:     `<svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/></svg>`,
        borrow:    `<svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>`,
        fines:     `<svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>`,
        students:  `<svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>`,
        profile:   `<svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>`,
        logout:    `<svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>`,
        manage:    `<svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>`,
    };

    /** Returns the navigation items for the given role. */
    function getNavItems(role) {
        const common = [
            { id: 'dashboard', label: 'Dashboard', icon: 'dashboard' },
            { id: 'catalog',   label: 'Book Catalog', icon: 'books' },
        ];

        const studentItems = [
            ...common,
            { id: 'borrows',  label: 'My Borrows', icon: 'borrow' },
            { id: 'fines',    label: 'My Fines', icon: 'fines' },
        ];

        const staffItems = [
            ...common,
            { id: 'borrows',   label: 'Circulation', icon: 'borrow' },
            { id: 'fines',     label: 'Fines', icon: 'fines' },
            { id: 'students',  label: 'Students', icon: 'students' },
            { id: 'books-mgmt', label: 'Manage Books', icon: 'manage' },
        ];

        if (role === 'STUDENT') return studentItems;
        return staffItems;
    }

    /** Renders the sidebar into the provided container element. */
    function render(container, activePage) {
        const role = Auth.getRole();
        const username = Auth.getUsername() || 'User';
        const items = getNavItems(role);
        const initial = username.charAt(0).toUpperCase();

        const sectionTitle = role === 'STUDENT' ? 'Library' : 'Administration';

        container.innerHTML = `
            <div class="sidebar-header">
                <img src="/assets/logo.png" alt="Library" class="sidebar-logo">
                <div class="sidebar-brand">
                    <span class="sidebar-brand-name">Central Library</span>
                    <span class="sidebar-brand-sub">Management System</span>
                </div>
                <button class="theme-toggle" id="theme-toggle" title="Toggle dark mode">${ThemeManager.getIcon()}</button>
            </div>
            <nav class="sidebar-nav">
                <div class="sidebar-section">
                    <div class="sidebar-section-title">${sectionTitle}</div>
                    ${items.map(item => `
                        <button class="nav-item ${activePage === item.id ? 'active' : ''}"
                                data-page="${item.id}">
                            ${ICONS[item.icon] || ''}
                            <span>${item.label}</span>
                        </button>
                    `).join('')}
                </div>
                <div class="sidebar-section">
                    <div class="sidebar-section-title">Account</div>
                    <button class="nav-item ${activePage === 'profile' ? 'active' : ''}"
                            data-page="profile">
                        ${ICONS.profile}
                        <span>Profile</span>
                    </button>
                    <button class="nav-item" data-action="logout">
                        ${ICONS.logout}
                        <span>Sign Out</span>
                    </button>
                </div>
            </nav>
            <div class="sidebar-footer">
                <div class="user-badge">
                    <div class="user-avatar">${initial}</div>
                    <div class="user-info">
                        <div class="user-name">${username}</div>
                        <div class="user-role">${role}</div>
                    </div>
                </div>
            </div>
        `;

        // Attach click handlers
        container.querySelectorAll('[data-page]').forEach(btn => {
            btn.addEventListener('click', () => App.navigate(btn.dataset.page));
        });
        container.querySelectorAll('[data-action="logout"]').forEach(btn => {
            btn.addEventListener('click', async () => {
                await Auth.logout();
                App.navigate('login');
            });
        });

        // Theme toggle
        const themeBtn = document.getElementById('theme-toggle');
        if (themeBtn) {
            themeBtn.addEventListener('click', () => {
                ThemeManager.toggle();
                themeBtn.textContent = ThemeManager.getIcon();
            });
        }
    }

    return { render };
})();
