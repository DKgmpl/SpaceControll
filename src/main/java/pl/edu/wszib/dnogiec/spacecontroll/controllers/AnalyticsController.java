package pl.edu.wszib.dnogiec.spacecontroll.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.edu.wszib.dnogiec.spacecontroll.services.AnalyticsService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Controller
@RequiredArgsConstructor
public class AnalyticsController {
    private final AnalyticsService analyticsService;

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
            Model model) {

//        int startHour = 8;
//        int endHour = 18;

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

        model.addAttribute("from", f);
        model.addAttribute("to", t);
        model.addAttribute("util", util);
        model.addAttribute("noshow", noshow);
        model.addAttribute("cancel", cancel);
        model.addAttribute("rightSizing", rightSizing);
        model.addAttribute("peakOccupancy", peakOccupancy);
        model.addAttribute("startHour", startHour);
        model.addAttribute("endHour", endHour);


        return "analytics";
    }
}
