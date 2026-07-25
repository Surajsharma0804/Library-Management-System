/**
 * Dashboard page — role-specific statistics cards and summary.
 */
const DashboardPage = (() => {
    async function render(container) {
        const role = Auth.getRole();
        container.innerHTML = `
            <div class="page-header">
                <h1>Dashboard</h1>
                <p class="subtitle">Welcome back, ${Auth.getUsername()}</p>
            </div>
            <div class="page-body">
                <div class="stats-grid" id="stats-grid">
                    <div class="loading-center"><div class="spinner"></div></div>
                </div>
            </div>
        `;

        try {
            const data = await API.get('/dashboard');
            renderStats(document.getElementById('stats-grid'), data, role);
        } catch (e) {
            Toast.error('Failed to load dashboard: ' + e.message);
        }
    }

    function renderStats(grid, data, role) {
        let cards = '';

        // Common cards
        cards += statCard('📚', 'Total Books', data.totalBooks || 0, 'blue');
        cards += statCard('✅', 'Available', data.availableBooks || 0, 'green');
        cards += statCard('📖', 'Borrowed', data.borrowedBooks || 0, 'orange');

        if (role === 'STUDENT') {
            cards += statCard('🔖', 'My Borrows', data.booksBorrowedByCurrentUser || 0, 'purple');
            cards += statCard('📋', 'Remaining Slots', data.remainingBorrowLimit || 0, 'accent');
            const fineBal = (data.currentUserFinePaise || 0) / 100;
            cards += statCard('💰', 'My Fines', '₹' + fineBal.toFixed(2), fineBal > 0 ? 'red' : 'green');
        } else {
            cards += statCard('⚠️', 'Overdue', data.overdueBooks || 0, 'red');
            cards += statCard('📝', 'Pending Reservations', data.pendingReservations || 0, 'purple');
            if (role === 'ADMIN') {
                cards += statCard('👥', 'Total Students', data.totalStudents || 0, 'accent');
                cards += statCard('🔔', 'Pending Fines', data.pendingFines || 0, 'orange');
                cards += statCard('👤', 'Active Students', data.activeStudents || 0, 'green');
            }
        }

        grid.innerHTML = cards;
    }

    function statCard(emoji, label, value, color) {
        return `
            <div class="stat-card">
                <div class="stat-icon ${color}">${emoji}</div>
                <div>
                    <div class="stat-value">${value}</div>
                    <div class="stat-label">${label}</div>
                </div>
            </div>
        `;
    }

    return { render };
})();
