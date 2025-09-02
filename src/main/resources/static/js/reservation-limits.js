(function () {
    function pad(n) { return n < 10 ? '0' + n : '' + n; }

    function parseLocal(dtstr) {
        if (!dtstr) return null;
        var parts = dtstr.split('T');
        if (parts.length !== 2) return null;
        var date = parts[0].split('-').map(function (n) { return parseInt(n, 10); });
        var time = parts[1].split(':').map(function (n) { return parseInt(n, 10); });
        return { y: date[0], m: date[1], d: date[2], hh: time[0], mm: time[1] };
    }

    function toInputValue(y, m, d, hh, mm) {
        return y + '-' + pad(m) + '-' + pad(d) + 'T' + pad(hh) + ':' + pad(mm);
    }

    function updateEndLimits(startInput, endInput) {
        var val = startInput.value;
        var parts = parseLocal(val);
        if (!parts) return;

        var min = val;
        var max = toInputValue(parts.y, parts.m, parts.d, 23, 59);

        endInput.min = min;
        endInput.max = max;

        if (endInput.value && (endInput.value < min || endInput.value > max)) {
            endInput.value = min;
        }
    }

    document.addEventListener('DOMContentLoaded', function () {
        var startInput = document.getElementById('startTime');
        var endInput = document.getElementById('endTime');
        if (!startInput || !endInput) return;

        updateEndLimits(startInput, endInput);
        startInput.addEventListener('change', function () {
            updateEndLimits(startInput, endInput);
        });
    });
})();