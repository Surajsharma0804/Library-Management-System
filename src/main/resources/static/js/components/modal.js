/**
 * Modal dialog component — renders a centered overlay dialog
 * with header, body, and optional footer actions.
 */
const Modal = (() => {
    let activeOverlay = null;

    function open({ title, content, actions = [] }) {
        close();

        const overlay = document.createElement('div');
        overlay.className = 'modal-overlay';
        overlay.addEventListener('click', (e) => {
            if (e.target === overlay) close();
        });

        const modal = document.createElement('div');
        modal.className = 'modal';

        // Header
        const header = document.createElement('div');
        header.className = 'modal-header';
        header.innerHTML = `<h2>${title}</h2>`;
        const closeBtn = document.createElement('button');
        closeBtn.className = 'modal-close';
        closeBtn.innerHTML = '&times;';
        closeBtn.addEventListener('click', close);
        header.appendChild(closeBtn);
        modal.appendChild(header);

        // Body
        const body = document.createElement('div');
        body.className = 'modal-body';
        if (typeof content === 'string') {
            body.innerHTML = content;
        } else {
            body.appendChild(content);
        }
        modal.appendChild(body);

        // Footer with actions
        if (actions.length > 0) {
            const footer = document.createElement('div');
            footer.className = 'modal-footer';
            actions.forEach(({ label, cls = 'btn-secondary', onClick }) => {
                const btn = document.createElement('button');
                btn.className = `btn ${cls}`;
                btn.textContent = label;
                btn.addEventListener('click', onClick);
                footer.appendChild(btn);
            });
            modal.appendChild(footer);
        }

        overlay.appendChild(modal);
        document.body.appendChild(overlay);
        activeOverlay = overlay;

        // Trap focus
        const firstInput = modal.querySelector('input, select, textarea, button');
        if (firstInput) firstInput.focus();

        // Close on Escape
        document.addEventListener('keydown', handleEsc);
    }

    function close() {
        if (activeOverlay) {
            activeOverlay.remove();
            activeOverlay = null;
        }
        document.removeEventListener('keydown', handleEsc);
    }

    function handleEsc(e) {
        if (e.key === 'Escape') close();
    }

    return { open, close };
})();
