package pl.edu.wszib.dnogiec.spacecontroll.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pl.edu.wszib.dnogiec.spacecontroll.services.AnalyticsService;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(MockitoExtension.class)
class AnalyticsExportStandaloneTest {
    @Mock AnalyticsService analyticsService;
    ObjectMapper objectMapper  = new ObjectMapper();
    MockMvc mvc;

    @BeforeEach
    void setup() {
        var controller = new AnalyticsController(analyticsService, objectMapper);
        // jeżeli controller pobiera godziny z @Value lub properties, ustaw w nim pola/reflection
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void exportAnalyticsCsv_ok() throws Exception {
        //stub metryk minimalnie
        when(analyticsService.utilizationBusinessHours(any(), any(), any(Integer.class), any(Integer.class)))
                .thenReturn(new AnalyticsService.UtilizationResult(100,1000,0.1));
        when(analyticsService.noShowRate(any(), any()))
                .thenReturn(new AnalyticsService.RateResult(1,10,0.1));
        when(analyticsService.cancellationRate(any(), any()))
                .thenReturn(new AnalyticsService.RateResult(2,10,0.2));
        when(analyticsService.autoReleaseRate(any(), any()))
                .thenReturn(new AnalyticsService.RateResult(1,10,0.1));
        when(analyticsService.rightSizing(any(), any()))
                .thenReturn(new AnalyticsService.RightSizingResult(3.5,8));
        when(analyticsService.peakOccupancy(any(), any()))
                .thenReturn(new AnalyticsService.PeakOccupancyResult(4, LocalDateTime.now()));

        mvc.perform(get("/analytics/export")
                .param("from", "2025-01-01T08:00")
                .param("to", "2025-01-02T18:00"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("analytics.csv")))
            .andExpect(content().contentType("text/csv;charset=UTF-8"))
            .andExpect(content().string(containsString("autoReleaseCount")));
    }
}
