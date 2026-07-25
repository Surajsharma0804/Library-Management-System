/**
 * Book management page — admin/librarian CRUD operations for books.
 */
const BooksMgmtPage = (() => {
    let allBooks = [];

    async function render(container) {
        container.innerHTML = `
            <div class="page-header">
                <h1>Manage Books</h1>
                <p class="subtitle">Add, edit, and manage the book inventory</p>
            </div>
            <div class="page-body">
                <div class="toolbar">
                    <div class="search-bar">
                        <span class="search-icon">🔍</span>
                        <input class="form-input" id="mgmt-search"
                               placeholder="Search books..." type="search">
                    </div>
                    <div class="toolbar-actions">
                        <button class="btn btn-primary" id="add-book-btn">Add Book</button>
                    </div>
                </div>
                <div id="mgmt-table">
                    <div class="loading-center"><div class="spinner"></div></div>
                </div>
            </div>
        `;

        document.getElementById('add-book-btn').addEventListener('click', showAddDialog);
        document.getElementById('mgmt-search').addEventListener('input', (e) => {
            const q = e.target.value.toLowerCase();
            if (!q) { renderTable(allBooks); return; }
            renderTable(allBooks.filter(b =>
                (b.title || '').toLowerCase().includes(q) ||
                (b.author || '').toLowerCase().includes(q) ||
                (b.isbn || '').toLowerCase().includes(q)
            ));
        });

        await loadBooks();
    }

    async function loadBooks() {
        try {
            allBooks = await API.get('/books');
            renderTable(allBooks);
        } catch (e) {
            Toast.error('Failed to load books: ' + e.message);
        }
    }

    function renderTable(books) {
        const target = document.getElementById('mgmt-table');
        if (books.length === 0) {
            target.innerHTML = `
                <div class="empty-state">
                    <div class="icon">📚</div>
                    <h3>No books in inventory</h3>
                    <p>Add your first book to get started</p>
                </div>
            `;
            return;
        }

        target.innerHTML = `
            <div class="table-wrapper">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Title</th>
                            <th>Author</th>
                            <th>ISBN</th>
                            <th>Qty</th>
                            <th>Available</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${books.map(b => `
                            <tr>
                                <td class="text-muted text-xs">${esc(b.id)}</td>
                                <td><strong>${esc(b.title)}</strong></td>
                                <td>${esc(b.author)}</td>
                                <td class="text-muted">${esc(b.isbn)}</td>
                                <td>${b.totalQuantity}</td>
                                <td>${b.availableQuantity}</td>
                                <td>
                                    <button class="btn btn-sm btn-danger"
                                            onclick="BooksMgmtPage.deleteBook('${b.id}')">Remove</button>
                                </td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
        `;
    }

    function showAddDialog() {
        const form = document.createElement('div');
        form.innerHTML = `
            <div class="form-group">
                <label class="form-label">Title *</label>
                <input class="form-input" id="book-title" placeholder="Book title">
            </div>
            <div class="form-group">
                <label class="form-label">Author *</label>
                <input class="form-input" id="book-author" placeholder="Author name">
            </div>
            <div class="form-group">
                <label class="form-label">ISBN *</label>
                <input class="form-input" id="book-isbn" placeholder="e.g. 978-3-16-148410-0">
            </div>
            <div class="form-group">
                <label class="form-label">Quantity *</label>
                <input class="form-input" id="book-qty" type="number" min="1" value="1">
            </div>
        `;

        Modal.open({
            title: 'Add New Book',
            content: form,
            actions: [
                { label: 'Cancel', onClick: () => Modal.close() },
                { label: 'Add Book', cls: 'btn-primary', onClick: async () => {
                    const title = document.getElementById('book-title').value.trim();
                    const author = document.getElementById('book-author').value.trim();
                    const isbn = document.getElementById('book-isbn').value.trim();
                    const qty = parseInt(document.getElementById('book-qty').value) || 1;

                    if (!title || !author || !isbn) {
                        Toast.error('Title, author, and ISBN are required');
                        return;
                    }

                    try {
                        await API.post('/books', { title, author, isbn, totalQuantity: qty });
                        Toast.success('Book added to inventory');
                        Modal.close();
                        await loadBooks();
                    } catch (e) {
                        Toast.error(e.message);
                    }
                }}
            ]
        });
    }

    async function deleteBook(bookId) {
        if (!confirm('Are you sure you want to remove this book?')) return;
        try {
            await API.delete(`/books/${bookId}`);
            Toast.success('Book removed');
            await loadBooks();
        } catch (e) {
            Toast.error(e.message);
        }
    }

    function esc(s) { return (s || '').replace(/</g, '&lt;').replace(/>/g, '&gt;'); }

    return { render, deleteBook };
})();
