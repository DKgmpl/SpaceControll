package pl.edu.wszib.dnogiec.spacecontroll.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.edu.wszib.dnogiec.spacecontroll.services.AnalyticsService;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    @GetMapping("/analytics")
    public String analytics(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to,
            Model model) {
        LocalDateTime f = from != null ? from : LocalDateTime.now().minusDays(7);
        LocalDateTime t = to != null ? to : LocalDateTime.now();

        var util = analyticsService.utilization(f, t);
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

        return "analytics";
    }
}
