document.addEventListener('DOMContentLoaded', function () {
// Tooltips
    var tooltips = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltips.forEach(function (el) { new bootstrap.Tooltip(el); });

// Popovers (jeśli kiedyś użyjesz)
    var popovers = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    popovers.forEach(function (el) { new bootstrap.Popover(el); });
});