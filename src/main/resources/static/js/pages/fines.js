/**
 * Fines page — Students see their fines; staff can collect payments.
 */
const FinesPage = (() => {
    async function render(container) {
        const role = Auth.getRole();
        const isStudent = role === 'STUDENT';

        container.innerHTML = `
            <div class="page-header">
                <h1>${isStudent ? 'My Fines' : 'Fine Management'}</h1>
                <p class="subtitle">${isStudent ? 'View your outstanding fines and payment history' : 'Manage and collect student fines'}</p>
            </div>
            <div class="page-body">
                <div id="fines-table">
                    <div class="loading-center"><div class="spinner"></div></div>
                </div>
            </div>
        `;

        await loadFines();
    }

    async function loadFines() {
        try {
            const fines = await API.get('/fines');
            renderTable(fines);
        } catch (e) {
            Toast.error('Failed to load fines: ' + e.message);
        }
    }

    function renderTable(fines) {
        const target = document.getElementById('fines-table');
        const isStudent = Auth.getRole() === 'STUDENT';

        if (fines.length === 0) {
            target.innerHTML = `
                <div class="empty-state">
                    <div class="icon">💰</div>
                    <h3>No fines</h3>
                    <p>${isStudent ? 'You have no outstanding fines' : 'No pending fines in the system'}</p>
                </div>
            `;
            return;
        }

        target.innerHTML = `
            <div class="table-wrapper">
                <table class="data-table">
                    <thead>
                        <tr>
                            ${!isStudent ? '<th>Student</th>' : ''}
                            <th>Amount</th>
                            <th>Reason</th>
                            <th>Date</th>
                            <th>Status</th>
                            ${!isStudent ? '<th>Action</th>' : ''}
                        </tr>
                    </thead>
                    <tbody>
                        ${fines.map(f => `
                            <tr>
                                ${!isStudent ? `<td>${esc(f.registrationNumber)}</td>` : ''}
                                <td><strong>₹${(f.amount || 0).toFixed(2)}</strong></td>
                                <td>${esc(f.reason || '-')}</td>
                                <td>${formatDate(f.createdAt)}</td>
                                <td>${fineBadge(f.status)}</td>
                                ${!isStudent && f.status === 'PENDING' ? `
                                    <td><button class="btn btn-sm btn-primary"
                                         onclick="FinesPage.payFine('${f.id}')">Collect</button></td>
                                ` : (!isStudent ? '<td>-</td>' : '')}
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
        `;
    }

    async function payFine(fineId) {
        try {
            await API.post(`/fines/${fineId}/pay`);
            Toast.success('Fine collected successfully');
            await loadFines();
        } catch (e) {
            Toast.error(e.message);
        }
    }

    function fineBadge(status) {
        if (status === 'PAID')    return '<span class="badge badge-green">Paid</span>';
        if (status === 'WAIVED')  return '<span class="badge badge-blue">Waived</span>';
        if (status === 'PENDING') return '<span class="badge badge-red">Pending</span>';
        return `<span class="badge badge-muted">${status || '-'}</span>`;
    }

    function formatDate(d) {
        if (!d) return '-';
        return new Date(d).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
    }

    function esc(s) { return (s || '').replace(/</g, '&lt;').replace(/>/g, '&gt;'); }

    return { render, payFine };
})();
