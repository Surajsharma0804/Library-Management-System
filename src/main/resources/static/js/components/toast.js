/**
 * Toast notification system — shows success, error, and info
 * messages with auto-dismiss and slide-in animation.
 */
const Toast = (() => {
    let container;

    function init() {
        if (container) return;
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    function show(message, type = 'info', duration = 4000) {
        init();
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        toast.textContent = message;
        container.appendChild(toast);

        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(100px)';
            toast.style.transition = 'all 300ms ease';
            setTimeout(() => toast.remove(), 300);
        }, duration);
    }

    return {
        success: (msg) => show(msg, 'success'),
        error:   (msg) => show(msg, 'error'),
        info:    (msg) => show(msg, 'info'),
    };
})();
