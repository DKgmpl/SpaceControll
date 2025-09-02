(function () {
// Zapamiętanie ostatniego zakresu dat w localStorage
    document.addEventListener('DOMContentLoaded', function () {
        var fromEl = document.getElementById('from');
        var toEl = document.getElementById('to');
        if (!fromEl || !toEl) return;

        var form = fromEl.form;
        var STORAGE_KEY = 'analytics-range';

        var params = new URLSearchParams(window.location.search);
        var hasParams = params.has('from') || params.has('to');

        if (!hasParams) {
            var saved = localStorage.getItem(STORAGE_KEY);
            if (saved) {
                try {
                    var obj = JSON.parse(saved);
                    if (obj && obj.from && obj.to) {
                        fromEl.value = obj.from;
                        toEl.value = obj.to;
                    }
                } catch (e) { /* ignore */ }
            }
        }

        if (form) {
            form.addEventListener('submit', function () {
                var data = { from: fromEl.value || '', to: toEl.value || '' };
                localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
            });
        }
    });
})();