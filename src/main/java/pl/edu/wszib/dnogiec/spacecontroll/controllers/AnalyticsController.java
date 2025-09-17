package pl.edu.wszib.dnogiec.spacecontroll.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.nio.charset.StandardCharsets;
import pl.edu.wszib.dnogiec.spacecontroll.services.AnalyticsService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequiredArgsConstructor
public class AnalyticsController {
    private final AnalyticsService analyticsService;
    private final ObjectMapper objectMapper;
    // Godziny pracy z properties
    @Value("${app.business-hours.start:8}")
    private int startHour;

    @Value("${app.business-hours.end:18}")
    private int endHour;

    @GetMapping("/analytics")
    public String analytics(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to,
            Model model) throws JsonProcessingException {

        LocalDate today = LocalDate.now();
        LocalTime workStart = LocalTime.of(startHour, 0);
        LocalTime workEnd = LocalTime.of(endHour, 0);

        //Ostatnie 7 dni roboczych od 8:00 sześć dni temu do dziś 18:00
        LocalDateTime defFrom = LocalDateTime.of(today.minusDays(6), workStart);
        LocalDateTime defTo = LocalDateTime.of(today, workEnd);
        LocalDateTime f = (from != null ? from : defFrom).withSecond(0).withNano(0);
        LocalDateTime t = (to != null ? to : defTo).withSecond(0).withNano(0);

        var util = analyticsService.utilizationBusinessHours(f, t, startHour, endHour);
        var noshow = analyticsService.noShowRate(f, t);
        var cancel = analyticsService.cancellationRate(f, t);
        var rightSizing = analyticsService.rightSizing(f, t);
        var peakOccupancy = analyticsService.peakOccupancy(f, t);
        var heatmap = analyticsService.utilizationHeatmap(f, t, startHour, endHour);

        model.addAttribute("from", f);
        model.addAttribute("to", t);
        model.addAttribute("startHour", startHour);
        model.addAttribute("endHour", endHour);
        model.addAttribute("util", util);
        model.addAttribute("noshow", noshow);
        model.addAttribute("cancel", cancel);
        model.addAttribute("rightSizing", rightSizing);
        model.addAttribute("peakOccupancy", peakOccupancy);

        String heatmapJson = objectMapper.writeValueAsString(heatmap);
        System.out.println("Heatmap JSON length =  " + heatmapJson.length()); //debug
        model.addAttribute("heatmapJson",heatmapJson);

        return "analytics";
    }

    @GetMapping("/analytics/export")
    public ResponseEntity<byte[]> exportAnalyticsCsv(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) throws JsonProcessingException {
        LocalDate today = LocalDate.now();
        LocalTime workStart = LocalTime.of(startHour, 0);
        LocalTime workEnd = LocalTime.of(endHour, 0);

        LocalDateTime defFrom = LocalDateTime.of(today.minusDays(6), workStart);
        LocalDateTime defTo = LocalDateTime.of(today, workEnd);
        LocalDateTime f = (from != null ? from : defFrom).withSecond(0).withNano(0);
        LocalDateTime t = (to != null ? to : defTo).withSecond(0).withNano(0);

        var util = analyticsService.utilizationBusinessHours(f, t, startHour, endHour);
        var noshow = analyticsService.noShowRate(f, t);
        var cancel = analyticsService.cancellationRate(f, t);
        var rightSizing = analyticsService.rightSizing(f, t);
        var peak = analyticsService.peakOccupancy(f, t);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // CSV z separatorem średnikowym (lepsze pod PL Excel)
        StringBuilder sb = new StringBuilder();
        sb.append("from;to;businessHours;usedMinutes;availableMinutes;utilization%;noShowCount;noShowTotal;noShow%;cancelCount;cancelTotal;cancel%;avgRightSizing;rightSizingSample;peak;peakAt\n");
        sb.append(fmt.format(f)).append(';')
                .append(fmt.format(t)).append(';')
                .append(startHour).append(":00-").append(endHour).append(":00").append(';')
                .append(util.usedMinutes()).append(';')
                .append(util.availableMinutes()).append(';')
                .append(String.format(java.util.Locale.US, "%.1f", util.utilization()*100)).append(';')
                .append(noshow.numerator()).append(';')
                .append(noshow.denominator()).append(';')
                .append(String.format(java.util.Locale.US, "%.1f", noshow.rate()*100)).append(';')
                .append(cancel.numerator()).append(';')
                .append(cancel.denominator()).append(';')
                .append(String.format(java.util.Locale.US, "%.1f", cancel.rate()*100)).append(';')
                .append(String.format(java.util.Locale.US, "%.1f", rightSizing.avgDifference())).append(';')
                .append(rightSizing.sampleSize()).append(';')
                .append(peak.peak()).append(';')
                .append(peak.at() != null ? fmt.format(peak.at()) : "").append('\n');

        // BOM dla Excela w Windows
        byte[] bom = new byte[] {(byte)0xEF,(byte)0xBB,(byte)0xBF};
        byte[] body = (new String(bom, StandardCharsets.UTF_8) + sb).getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=analytics.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);

    }
}
