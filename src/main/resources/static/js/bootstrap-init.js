document.addEventListener('DOMContentLoaded', function () {
    if (typeof window.bootstrap === 'undefined') {
// console.warn('Bootstrap JS not loaded; skipping'); // opcjonalnie
        return;
    }
    var tooltips = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltips.forEach(function (el) { new bootstrap.Tooltip(el); });

    var popovers = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    popovers.forEach(function (el) { new bootstrap.Popover(el); });
});