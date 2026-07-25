/**
 * Theme manager — handles dark/light mode toggle
 * with localStorage persistence.
 */
const ThemeManager = (() => {
    const STORAGE_KEY = 'lms-theme';

    function getPreferred() {
        const saved = localStorage.getItem(STORAGE_KEY);
        if (saved === 'dark' || saved === 'light') return saved;
        return 'light';
    }

    function apply(theme) {
        document.documentElement.setAttribute('data-theme', theme);
        localStorage.setItem(STORAGE_KEY, theme);
    }

    function toggle() {
        const current = document.documentElement.getAttribute('data-theme') || 'light';
        apply(current === 'dark' ? 'light' : 'dark');
    }

    function isDark() {
        return (document.documentElement.getAttribute('data-theme') || 'light') === 'dark';
    }

    function getIcon() {
        return isDark() ? '☀️' : '🌙';
    }

    // Apply on load
    apply(getPreferred());

    return { toggle, isDark, getIcon, apply };
})();
