package pl.edu.wszib.dnogiec.spacecontroll.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.wszib.dnogiec.spacecontroll.dao.impl.spring.data.ReservationRepository;
import pl.edu.wszib.dnogiec.spacecontroll.model.ConferenceRoom;
import pl.edu.wszib.dnogiec.spacecontroll.model.Reservation;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    ReservationRepository repo;

    @Mock
    IConferenceRoomService roomService;

    @InjectMocks
    AnalyticsService analytics;

    @BeforeEach
    void resetMocks() {
        Mockito.reset(repo, roomService);
    }

    private Reservation res(LocalDateTime s, LocalDateTime e, Reservation.ReservationStatus st) {
        var r = new Reservation();
        r.setStartTime(s);
        r.setEndTime(e);
        r.setStatus(st);
        return r;
    }

    @Test
    void utilizationBusinessHours_clipsToWindowAndBusinessHours() {
        var from = LocalDateTime.of(2025, 1, 1, 8, 0);
        var to = LocalDateTime.of(2025, 1, 1, 18, 0);

        //jedna rezerwacja 7:30-8:30 => w godzinach pracy liczy się tylko 8:00-8:30 = 30 min
        var r1 = res(LocalDateTime.of(2025, 1, 1, 7, 30),
                     LocalDateTime.of(2025, 1, 1, 8, 30),
                     Reservation.ReservationStatus.COMPLETED);

        when(repo.findAll()).thenReturn(List.of(r1));
        when(roomService.getAllRooms()).thenReturn(List.of(new ConferenceRoom(), new ConferenceRoom())); // 2 sale

        var out = analytics.utilizationBusinessHours(from, to, 8, 18);
        assertThat(out.usedMinutes()).isEqualTo(30);
        assertThat(out.availableMinutes()).isEqualTo(2L * 600L); //2 sale * 600 min
        assertThat(out.utilization()).isEqualTo(30.0 / 1200.0);
    }

    @Test
    void heatmap_dimensionsAndBasicValue() {
        var from = LocalDateTime.of(2025, 1, 1, 8, 0); //środa
        var to = LocalDateTime.of(2025, 1, 1, 10, 0);

        //pełna godzina 8-9 zajęta jedną rezerwacją
        var r = res(LocalDateTime.of(2025, 1, 1, 8, 0),
                    LocalDateTime.of(2025, 1, 1, 9, 0),
                    Reservation.ReservationStatus.ACTIVE);

        when(repo.findAll()).thenReturn(List.of(r));
        when(roomService.getAllRooms()).thenReturn(List.of(new ConferenceRoom())); //1 sala

        var h = analytics.utilizationHeatmap(from, to, 8, 10);
        assertThat(h.days()).hasSize(7);
        assertThat(h.hours()).containsExactly("08:00", "09:00");
        assertThat(h.values()).hasDimensions(7, 2);

        //Środa to index 2 (Pon=0, Wt=1, Śr=2, ...) godz 8-9 to kolumna 0
        double v = h.values()[2][0];
        assertThat(v).isEqualTo(1.0); //100% w tej godzinie
    }
}
