(function () {
// paleta (YlOrRd) + wzmocniony kontrast
    var STOPS = [
        { v: 0.00, r: 255, g: 255, b: 229 },
        { v: 0.20, r: 255, g: 247, b: 188 },
        { v: 0.40, r: 254, g: 227, b: 145 },
        { v: 0.60, r: 254, g: 196, b: 79  },
        { v: 0.80, r: 251, g: 154, b: 41  },
        { v: 1.00, r: 204, g: 76,  b: 2   }
    ];
    function lerp(a, b, t) { return a + (b - a) * t; }
    function colorScale(v) {
        var vv = Math.pow(Math.max(0, Math.min(1, v)), 0.8);
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
        if (!canvas || !dataEl || typeof Chart === 'undefined') return;

        var raw = (dataEl.textContent || dataEl.innerText || '').trim();
        if (!raw) return;
        var data = JSON.parse(raw);
        if (!data || !data.days || !data.hours || !data.values) return;

// UWAGA: Zamiana osi
// x = indeks dnia (0..6), y = indeks godziny (0..hours-1)
        var points = [];
        for (var d = 0; d < data.days.length; d++) {
            var row = data.values[d] || [];
            for (var h = 0; h < data.hours.length; h++) {
                var v = row[h] || 0;
                points.push({ x: d, y: h, v: v });
            }
        }

        var PADDING = { top: 34, right: 56, bottom: 28, left: 64 };
        var ctx = canvas.getContext('2d');
        new Chart(ctx, {
            type: 'matrix',
            data: {
                datasets: [{
                    label: 'Utilization heatmap',
                    data: points,
                    backgroundColor: function (ctx) {
                        return colorScale(ctx.raw.v);
                    },
                    borderWidth: 0.5,
                    borderColor: 'rgba(255,255,255,0.9)',
                    // Szerokość komórki: dziel przez liczbę DNI (x-ów)
                    width: function (ctx) {
                        var area = ctx.chart.chartArea;
                        if (!area) return 20;
                        return (area.right - area.left) / data.days.length - 1;
                    },
                    // Wysokość komórki: dziel przez liczbę GODZIN (y-ów)
                    height: function (ctx) {
                        var area = ctx.chart.chartArea;
                        if (!area) return 20;
                        return (area.bottom - area.top) / data.hours.length - 2;
                    }
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                layout: { padding: PADDING },
                plugins: {
                    tooltip: {
                        callbacks: {
                            title: function () { return ''; },
                            label: function (ctx) {
                                var dayIdx = ctx.raw.x, hourIdx = ctx.raw.y, v = ctx.raw.v;
                                var pct = Math.round(v * 1000) / 10;
                                return data.days[dayIdx] + ', ' + data.hours[hourIdx] + ': ' + pct + '%';
                            }
                        }
                    },
                    legend: { display: false }
                },
                scales: {
                    // Oś X: dni
                    x: {
                        type: 'linear',
                        position: 'top',
                        offset: true,
                        grid: { display: false },
                        ticks: {
                            padding: 6,
                            autoSkip: false,
                            maxRotation: 0,
                            minRotation: 0,
                            callback: function (val) {
                                var i = Number(val);
                                return data.days[i] || '';
                            }
                        }
                    },
                    // Oś Y: godziny (rosną w dół)
                    y: {
                        type: 'linear',
                        reverse: true,
                        offset: true,
                        grid: { display: false },
                        ticks: {
                            padding: 6,
                            autoSkip: false,
                            callback: function (val) {
                                var i = Number(val);
                                return data.hours[i] || '';
                            }
                        }
                    }
                }
            }
        });
    });
})();