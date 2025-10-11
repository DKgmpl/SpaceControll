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

    // No‑show – weekendy wykluczane, gdy flaga aktywna
    @Test
    void noShow_excludesWeekendsWhenFlagTrue() {
        var sunEnd = LocalDateTime.of(2025,1,5,12,0); // niedziela
        var friEnd = LocalDateTime.of(2025,1,3,12,0); // piątek

        Reservation rSun = new Reservation();
        rSun.setStatus(Reservation.ReservationStatus.COMPLETED);
        rSun.setStartTime(sunEnd.minusHours(1)); rSun.setEndTime(sunEnd);
        rSun.setCheckInTime(null);

        Reservation rFri = new Reservation();
        rFri.setStatus(Reservation.ReservationStatus.COMPLETED);
        rFri.setStartTime(friEnd.minusHours(1)); rFri.setEndTime(friEnd);
        rFri.setCheckInTime(null);

        when(repo.findAll()).thenReturn(List.of(rSun, rFri));

        var from = LocalDateTime.of(2025,1,1,0,0);
        var to   = LocalDateTime.of(2025,1,10,0,0);

        var withW = analytics.noShowRate(from,to,false);
        var withoutW = analytics.noShowRate(from,to,true);

        assertThat(withW.denominator()).isEqualTo(2);
        assertThat(withoutW.denominator()).isEqualTo(1);
    }

    // Cancellation – liczone po createdAt (i wykluczane weekendy)
    @Test
    void cancellation_excludesWeekendsByCreatedAt() {
        var satCreated = LocalDateTime.of(2025,1,4,9,0); // sobota
        var monCreated = LocalDateTime.of(2025,1,6,9,0); // poniedziałek

        Reservation a = new Reservation();
        a.setStatus(Reservation.ReservationStatus.CANCELLED);
        a.setCreatedAt(satCreated);

        Reservation b = new Reservation();
        b.setStatus(Reservation.ReservationStatus.CANCELLED);
        b.setCreatedAt(monCreated);

        when(repo.findAll()).thenReturn(List.of(a,b));

        var from = LocalDateTime.of(2025,1,1,0,0);
        var to   = LocalDateTime.of(2025,1,10,0,0);

        var withW = analytics.cancellationRate(from,to,false);
        var withoutW = analytics.cancellationRate(from,to,true);

        assertThat(withW.numerator()).isEqualTo(2);
        assertThat(withoutW.numerator()).isEqualTo(1);
    }

    // Peak occupancy – maksimum 2 dla dwóch nakładających się rezerwacji
    @Test
    void peakOccupancy_detectsOverlapPeak2() {
        var from = LocalDateTime.of(2025,1,2,8,0);
        var to   = LocalDateTime.of(2025,1,2,12,0);

        var r1 = new Reservation(); r1.setStatus(Reservation.ReservationStatus.ACTIVE);
        r1.setStartTime(LocalDateTime.of(2025,1,2,9,0));
        r1.setEndTime(LocalDateTime.of(2025,1,2,11,0));

        var r2 = new Reservation(); r2.setStatus(Reservation.ReservationStatus.ACTIVE);
        r2.setStartTime(LocalDateTime.of(2025,1,2,10,0));
        r2.setEndTime(LocalDateTime.of(2025,1,2,12,0));

        when(repo.findAll()).thenReturn(List.of(r1, r2));

        var peak = analytics.peakOccupancy(from,to,false);
        assertThat(peak.peak()).isEqualTo(2);
    }

    // Heatmapa – wymiary [7 × N] i wartości w [0,1]
    @Test
    void heatmap_dimensionsAndRange() {
        var from = LocalDateTime.of(2025,1,1,8,0);
        var to   = LocalDateTime.of(2025,1,1,10,0); // 2 godziny

        var r = new Reservation();
        r.setStatus(Reservation.ReservationStatus.ACTIVE);
        r.setStartTime(LocalDateTime.of(2025,1,1,8,30));
        r.setEndTime(LocalDateTime.of(2025,1,1,9,0));

        when(repo.findAll()).thenReturn(List.of(r));
        when(roomService.getAllRooms()).thenReturn(List.of(new ConferenceRoom())); // 1 sala

        var h = analytics.utilizationHeatmap(from,to,8,10,true);
        assertThat(h.days()).hasSize(7);
        assertThat(h.hours()).containsExactly("08:00","09:00");
        assertThat(h.values()).hasDimensions(7,2);
        // wartości w 0..1
        for (double[] row : h.values()) for (double v : row) assertThat(v).isBetween(0.0, 1.0);
    }


}
