package pl.edu.wszib.dnogiec.spacecontroll.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pl.edu.wszib.dnogiec.spacecontroll.services.AnalyticsService;

import java.time.LocalDateTime;
import java.util.Locale;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(MockitoExtension.class)
class AnalyticsExportStandaloneTest {
    @Mock
    AnalyticsService analyticsService;
    ObjectMapper objectMapper = new ObjectMapper();
    MockMvc mvc;
    AnalyticsController controller;

    @BeforeEach
    void setup() throws Exception {
        Locale.setDefault(Locale.US);
        controller = new AnalyticsController(analyticsService, objectMapper);

        // wstrzyknięcie wartości pól @Value via refleksja
        setField(controller, "startHour", 8);
        setField(controller, "endHour", 18);
        setField(controller, "excludeWeekends", true);

        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        var f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }


    @Test
    void exportAnalyticsCsv_containsWeekendFlagAndLeadTime() throws Exception {
        //stub metryk minimalnie
        when(analyticsService.utilizationBusinessHours(any(), any(), anyInt(), anyInt(), eq(true)))
                .thenReturn(new AnalyticsService.UtilizationResult(100, 1000, 0.1));
        when(analyticsService.noShowRate(any(), any(), eq(true)))
                .thenReturn(new AnalyticsService.RateResult(1, 10, 0.1));
        when(analyticsService.cancellationRate(any(), any(), eq(true)))
                .thenReturn(new AnalyticsService.RateResult(2, 10, 0.2));
        when(analyticsService.autoReleaseRate(any(), any(), eq(true)))
                .thenReturn(new AnalyticsService.RateResult(1, 10, 0.1));
        when(analyticsService.rightSizing(any(), any(), eq(true)))
                .thenReturn(new AnalyticsService.RightSizingResult(3.5, 8));
        when(analyticsService.peakOccupancy(any(), any(), eq(true)))
                .thenReturn(new AnalyticsService.PeakOccupancyResult(4, LocalDateTime.now()));
        when(analyticsService.leadTime(any(), any(), eq(true)))
                .thenReturn(new AnalyticsService.LeadTimeResult(24.0, 5));

        mvc.perform(get("/analytics/export")
                        .param("from", "2025-01-01T08:00")
                        .param("to", "2025-01-02T18:00"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("analytics.csv")))
                .andExpect(content().contentType("text/csv;charset=UTF-8"))
                .andExpect(content().string(containsString("weekendsExcluded;true")))
                .andExpect(content().string(containsString("leadTimeAvgHours")))
                .andExpect(content().string(containsString("24.0")));
    }
}
