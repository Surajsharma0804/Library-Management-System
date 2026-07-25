/**
 * Borrows page — Students see their borrow history.
 * Staff see all active borrows with issue/return controls.
 */
const BorrowsPage = (() => {
    async function render(container) {
        const role = Auth.getRole();
        const isStudent = role === 'STUDENT';
        const title = isStudent ? 'My Borrows' : 'Circulation';
        const subtitle = isStudent
            ? 'View your current and past book borrows'
            : 'Issue and return books for students';

        container.innerHTML = `
            <div class="page-header">
                <h1>${title}</h1>
                <p class="subtitle">${subtitle}</p>
            </div>
            <div class="page-body">
                ${!isStudent ? `
                <div class="toolbar">
                    <div></div>
                    <div class="toolbar-actions">
                        <button class="btn btn-primary" id="issue-btn">Issue Book</button>
                    </div>
                </div>
                ` : ''}
                <div id="borrows-table">
                    <div class="loading-center"><div class="spinner"></div></div>
                </div>
            </div>
        `;

        if (!isStudent) {
            document.getElementById('issue-btn').addEventListener('click', showIssueDialog);
        }

        await loadBorrows();
    }

    async function loadBorrows() {
        try {
            const records = await API.get('/borrows');
            renderTable(records);
        } catch (e) {
            Toast.error('Failed to load borrows: ' + e.message);
        }
    }

    function renderTable(records) {
        const target = document.getElementById('borrows-table');
        const role = Auth.getRole();
        const isStudent = role === 'STUDENT';

        if (records.length === 0) {
            target.innerHTML = `
                <div class="empty-state">
                    <div class="icon">📖</div>
                    <h3>No borrow records</h3>
                    <p>${isStudent ? 'You have no active borrows' : 'No active borrows in the system'}</p>
                </div>
            `;
            return;
        }

        target.innerHTML = `
            <div class="table-wrapper">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Book</th>
                            ${!isStudent ? '<th>Student</th>' : ''}
                            <th>Issue Date</th>
                            <th>Due Date</th>
                            <th>Status</th>
                            ${!isStudent ? '<th>Action</th>' : ''}
                        </tr>
                    </thead>
                    <tbody>
                        ${records.map(r => `
                            <tr>
                                <td><strong>${esc(r.bookTitle)}</strong></td>
                                ${!isStudent ? `<td>${esc(r.registrationNumber)}</td>` : ''}
                                <td>${formatDate(r.issueDate)}</td>
                                <td>${formatDate(r.dueDate)}</td>
                                <td>${borrowBadge(r)}</td>
                                ${!isStudent && r.status === 'ACTIVE' ? `
                                    <td><button class="btn btn-sm btn-secondary"
                                         onclick="BorrowsPage.returnBook('${r.id}')">Return</button></td>
                                ` : (!isStudent ? '<td>-</td>' : '')}
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
        `;
    }

    function showIssueDialog() {
        const form = document.createElement('div');
        form.innerHTML = `
            <div class="form-group">
                <label class="form-label">Book ID</label>
                <input class="form-input" id="issue-book-id" placeholder="e.g. BOOK-000001">
            </div>
            <div class="form-group">
                <label class="form-label">Student Registration No.</label>
                <input class="form-input" id="issue-reg-no" placeholder="e.g. REG-000001">
            </div>
        `;

        Modal.open({
            title: 'Issue Book',
            content: form,
            actions: [
                { label: 'Cancel', onClick: () => Modal.close() },
                { label: 'Issue', cls: 'btn-primary', onClick: async () => {
                    const bookId = document.getElementById('issue-book-id').value.trim();
                    const regNo = document.getElementById('issue-reg-no').value.trim();
                    if (!bookId || !regNo) {
                        Toast.error('Both Book ID and Registration Number are required');
                        return;
                    }
                    try {
                        await API.post('/borrows/issue', { bookId, registrationNumber: regNo });
                        Toast.success('Book issued successfully');
                        Modal.close();
                        await loadBorrows();
                    } catch (e) {
                        Toast.error(e.message);
                    }
                }}
            ]
        });
    }

    async function returnBook(borrowId) {
        try {
            await API.post('/borrows/return', { borrowId });
            Toast.success('Book returned successfully');
            await loadBorrows();
        } catch (e) {
            Toast.error(e.message);
        }
    }

    function borrowBadge(r) {
        const s = r.status || '';
        if (s === 'ACTIVE') {
            const due = new Date(r.dueDate);
            if (due < new Date()) return '<span class="badge badge-red">Overdue</span>';
            return '<span class="badge badge-blue">Active</span>';
        }
        if (s === 'RETURNED') return '<span class="badge badge-green">Returned</span>';
        return `<span class="badge badge-muted">${s}</span>`;
    }

    function formatDate(d) {
        if (!d) return '-';
        return new Date(d).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
    }

    function esc(s) { return (s || '').replace(/</g, '&lt;').replace(/>/g, '&gt;'); }

    return { render, returnBook };
})();
