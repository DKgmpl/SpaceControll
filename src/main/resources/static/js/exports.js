(function () {
    function byId(id) {
        return document.getElementById(id);
    }

    function syncRanges() {
        var from = byId('rangeFrom')?.value || '';
        var to = byId('rangeTo')?.value || '';
        if (byId('resFrom')) byId('resFrom').value = from;
        if (byId('resTo')) byId('resTo').value = to;
        if (byId('anFrom')) byId('anFrom').value = from;
        if (byId('anTo')) byId('anTo').value = to;
    }

    function toLocalDateTimeString(dt) {
// format yyyy-MM-ddTHH:mm
        function pad(n) {
            return n < 10 ? '0' + n : '' + n;
        }

        return dt.getFullYear() + '-' + pad(dt.getMonth() + 1) + '-' + pad(dt.getDate())
            + 'T' + pad(dt.getHours()) + ':' + pad(dt.getMinutes());
    }

    function setPreset(preset) {
        var now = new Date();
        var from, to;

        if (preset === 'last7') {
            var f = new Date(now);
            f.setDate(now.getDate() - 6);
            from = new Date(f.getFullYear(), f.getMonth(), f.getDate(), 0, 0, 0, 0);
            to = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 0, 0);
        } else if (preset === 'thisMonth') {
            from = new Date(now.getFullYear(), now.getMonth(), 1, 0, 0, 0, 0);
            to = new Date(now.getFullYear(), now.getMonth() + 1, 0, 23, 59, 0, 0); // ostatni dzień miesiąca
        } else if (preset === 'prevWeek') {
            // tydzień = poprzednie 7 dni pełnych
            var end = new Date(now);
            end.setDate(now.getDate() - 7);
            var start = new Date(end);
            start.setDate(end.getDate() - 6);
            from = new Date(start.getFullYear(), start.getMonth(), start.getDate(), 0, 0, 0, 0);
            to = new Date(end.getFullYear(), end.getMonth(), end.getDate(), 23, 59, 0, 0);
        } else {
            return;
        }

        byId('rangeFrom').value = toLocalDateTimeString(from);
        byId('rangeTo').value = toLocalDateTimeString(to);
        syncRanges();
    }

    document.addEventListener('DOMContentLoaded', function () {
// Prezety
        document.querySelectorAll('[data-preset]').forEach(function (btn) {
            btn.addEventListener('click', function () {
                setPreset(btn.getAttribute('data-preset'));
            });
        });

// Na starcie skopiuj aktualne wartości do hidden inputs
        syncRanges();

// Utrzymuj hidden inputs w sync z polami zakresu
        var rf = byId('rangeFrom'), rt = byId('rangeTo');
        if (rf) rf.addEventListener('change', syncRanges);
        if (rt) rt.addEventListener('change', syncRanges);

// Synchronizuj przed submitem
        var fr = byId('form-reservations'), fa = byId('form-analytics');
        if (fr) fr.addEventListener('submit', syncRanges);
        if (fa) fa.addEventListener('submit', syncRanges);
    });
})();