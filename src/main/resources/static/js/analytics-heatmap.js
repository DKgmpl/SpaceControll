(function () {
// Wielostopniowa skala kolorów (YlOrRd)
    var STOPS = [
        {v: 0.00, r: 255, g: 255, b: 229}, // #ffffe5
        {v: 0.20, r: 255, g: 247, b: 188}, // #fff7bc
        {v: 0.40, r: 254, g: 227, b: 145}, // #fee391
        {v: 0.60, r: 254, g: 196, b: 79}, // #fec44f
        {v: 0.80, r: 251, g: 154, b: 41}, // #fb9a29
        {v: 1.00, r: 204, g: 76, b: 2}  // #cc4c02
    ];

    function lerp(a, b, t) {
        return a + (b - a) * t;
    }

    function colorScale(v) {
// lekkie „rozciągnięcie” kontrastu w górę
        var vv = Math.pow(Math.min(Math.max(v, 0), 1), 0.8);
// znajdź segment
        for (var i = 0; i < STOPS.length - 1; i++) {
            var a = STOPS[i], b = STOPS[i + 1];
            if (vv >= a.v && vv <= b.v) {
                var t = (vv - a.v) / (b.v - a.v);
                var r = Math.round(lerp(a.r, b.r, t));
                var g = Math.round(lerp(a.g, b.g, t));
                var b2 = Math.round(lerp(a.b, b.b, t));
                return 'rgb(' + r + ',' + g + ',' + b2 + ')';
            }
        }
        var last = STOPS[STOPS.length - 1];
        return 'rgb(' + last.r + ',' + last.g + ',' + last.b + ')';
    }

    document.addEventListener('DOMContentLoaded', function () {
        var dataEl = document.getElementById('heatmap-data');
        var canvas = document.getElementById('util-heatmap');
        if (!canvas) return;
        if (typeof Chart === 'undefined') {
            console.error('Chart.js nie jest załadowany');
            return;
        }
        if (!dataEl) {
            console.warn('Brak elementu heatmap-data');
            return;
        }

        var raw = (dataEl.textContent || dataEl.innerText || '').trim();
        if (!raw) {
            console.warn('Brak danych heatmapy.');
            return;
        }

        var data;
        try {
            data = JSON.parse(raw);
        } catch (e) {
            console.error('Błąd JSON heatmapy:', e);
            return;
        }
        if (!data || !data.days || !data.hours || !data.values) {
            console.warn('Brak wymaganych pól w danych heatmapy.');
            return;
        }

// Przygotuj punkty
        var points = [];
        for (var y = 0; y < data.days.length; y++) {
            var row = data.values[y] || [];
            for (var x = 0; x < data.hours.length; x++) {
                var v = row[x] || 0;
                points.push({x: x, y: y, v: v});
            }
        }

// Dodatkowy padding, aby etykiety osi nie nachodziły na pierwszy wiersz
        var PADDING = {top: 40, right: 24, bottom: 28, left: 64};

        var ctx = canvas.getContext('2d');
        new Chart(ctx, {
            type: 'matrix',
            data: {
                datasets: [{
                    label: 'Utilization heatmap',
                    data: points,
                    backgroundColor: function (ctx) {
                        var v = ctx.raw.v;
                        return colorScale(Math.max(0, Math.min(1, v)));
                    },
                    borderWidth: 1,
                    borderColor: 'rgba(255,255,255,0.9)',
                    width: function (ctx) {
                        var area = ctx.chart.chartArea;
                        if (!area) return 20;
                        // szerokość „komórki” z małym odstępem
                        return (area.right - area.left) / data.hours.length - 2;
                    },
                    height: function (ctx) {
                        var area = ctx.chart.chartArea;
                        if (!area) return 20;
                        // wysokość „komórki” z małym odstępem
                        return (area.bottom - area.top) / data.days.length - 2;
                    }
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                layout: {padding: PADDING},
                plugins: {
                    tooltip: {
                        callbacks: {
                            title: function () {
                                return '';
                            },
                            label: function (ctx) {
                                var x = ctx.raw.x, y = ctx.raw.y, v = ctx.raw.v;
                                var pct = Math.round(v * 1000) / 10;
                                return data.days[y] + ', ' + data.hours[x] + ': ' + pct + '%';
                            }
                        }
                    },
                    legend: {display: false}
                },
                scales: {
                    x: {
                        type: 'linear',
                        position: 'top',
                        offset: true,                 // odsuń siatkę od krawędzi
                        grid: {display: false},
                        ticks: {
                            padding: 6,                 // dystans etykiet od pola rysowania
                            autoSkip: false,
                            maxRotation: 0,
                            minRotation: 0,
                            callback: function (val) {
                                var i = Number(val);
                                return data.hours[i] || '';
                            }
                        }
                    },
                    y: {
                        type: 'linear',
                        reverse: true,                // Pon u góry
                        offset: true,
                        grid: {display: false},
                        ticks: {
                            padding: 6,
                            autoSkip: false,
                            callback: function (val) {
                                var i = Number(val);
                                return data.days[i] || '';
                            }
                        }
                    }
                }
            }
        });
    });
})();