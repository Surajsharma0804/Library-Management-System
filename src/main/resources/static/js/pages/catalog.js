/**
 * Book Catalog page — searchable book listing available to all roles.
 */
const CatalogPage = (() => {
    let allBooks = [];

    async function render(container) {
        container.innerHTML = `
            <div class="page-header">
                <h1>Book Catalog</h1>
                <p class="subtitle">Browse and search the library collection</p>
            </div>
            <div class="page-body">
                <div class="toolbar">
                    <div class="search-bar">
                        <span class="search-icon">🔍</span>
                        <input class="form-input" id="catalog-search"
                               placeholder="Search by title, author, or ISBN..." type="search">
                    </div>
                </div>
                <div id="catalog-table">
                    <div class="loading-center"><div class="spinner"></div></div>
                </div>
            </div>
        `;

        try {
            allBooks = await API.get('/books');
            renderTable(allBooks);
        } catch (e) {
            Toast.error('Failed to load catalog: ' + e.message);
        }

        document.getElementById('catalog-search').addEventListener('input', (e) => {
            const q = e.target.value.toLowerCase();
            if (!q) {
                renderTable(allBooks);
                return;
            }
            const filtered = allBooks.filter(b =>
                (b.title || '').toLowerCase().includes(q) ||
                (b.author || '').toLowerCase().includes(q) ||
                (b.isbn || '').toLowerCase().includes(q)
            );
            renderTable(filtered);
        });
    }

    function renderTable(books) {
        const target = document.getElementById('catalog-table');
        if (books.length === 0) {
            target.innerHTML = `
                <div class="empty-state">
                    <div class="icon">📚</div>
                    <h3>No books found</h3>
                    <p>Try adjusting your search terms</p>
                </div>
            `;
            return;
        }

        target.innerHTML = `
            <div class="table-wrapper">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Title</th>
                            <th>Author</th>
                            <th>ISBN</th>
                            <th>Category</th>
                            <th>Available</th>
                            <th>Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${books.map(b => `
                            <tr>
                                <td><strong>${esc(b.title)}</strong></td>
                                <td>${esc(b.author)}</td>
                                <td class="text-muted">${esc(b.isbn)}</td>
                                <td>${esc(b.category || '-')}</td>
                                <td>${b.availableQuantity} / ${b.totalQuantity}</td>
                                <td>${statusBadge(b)}</td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
        `;
    }

    function statusBadge(b) {
        if (b.availableQuantity > 0) return '<span class="badge badge-green">Available</span>';
        return '<span class="badge badge-red">Unavailable</span>';
    }

    function esc(s) { return (s || '').replace(/</g, '&lt;').replace(/>/g, '&gt;'); }

    return { render };
})();
