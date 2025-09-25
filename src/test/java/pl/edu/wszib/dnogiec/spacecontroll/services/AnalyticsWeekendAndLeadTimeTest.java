package pl.edu.wszib.dnogiec.spacecontroll.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.wszib.dnogiec.spacecontroll.dao.impl.spring.data.ReservationRepository;
import pl.edu.wszib.dnogiec.spacecontroll.model.ConferenceRoom;
import pl.edu.wszib.dnogiec.spacecontroll.model.Reservation;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsWeekendAndLeadTimeTest {
    @Mock ReservationRepository repo;
    @Mock IConferenceRoomService roomService;
    @InjectMocks AnalyticsService analytics;

    @BeforeEach
    void setUp() {
        Mockito.reset(repo, roomService);
        Mockito.lenient().when(roomService.getAllRooms()).thenReturn(List.of(new ConferenceRoom())); // 1 sala
    }

    private Reservation res(LocalDateTime start, LocalDateTime end, Reservation.ReservationStatus st) {
        Reservation r = new Reservation();
        r.setStartTime(start);
        r.setEndTime(end);
        r.setStatus(st);
        return r;
    }

    @Test
    void cancellationRate_excludeWeekends_whenFlagTrue() {
        // createdAt w sobotę i w poniedziałek, oba w oknie
        var sat = LocalDateTime.of(2025, 1, 4, 12, 0);  // sobota
        var mon = LocalDateTime.of(2025, 1, 6, 9, 0);   // poniedziałek

        Reservation rSat = new Reservation();
        rSat.setStatus(Reservation.ReservationStatus.CANCELLED);
        rSat.setCreatedAt(sat);

        Reservation rMon = new Reservation();
        rMon.setStatus(Reservation.ReservationStatus.CANCELLED);
        rMon.setCreatedAt(mon);

        when(repo.findAll()).thenReturn(List.of(rSat, rMon));

        var from = LocalDateTime.of(2025, 1, 1, 0, 0);
        var to = LocalDateTime.of(2025, 1, 10, 0, 0);

        var withWeekends = analytics.cancellationRate(from, to, false);
        var withoutWeekends = analytics.cancellationRate(from, to, true);

        // z weekendami: 2/2, bez weekendów: tylko poniedziałek 1/1
        assertThat(withWeekends.numerator()).isEqualTo(2);
        assertThat(withWeekends.denominator()).isEqualTo(2);
        assertThat(withoutWeekends.numerator()).isEqualTo(1);
        assertThat(withoutWeekends.denominator()).isEqualTo(1);
    }

    @Test
    void noShowRate_excludeWeekends_whenFlagTrue() {
        // COMPLETED bez check-in: niedziela (powinien wypaść przy excludeWeekends)
        var sunEnd = LocalDateTime.of(2025, 1, 5, 12, 0); // niedziela
        var friEnd = LocalDateTime.of(2025, 1, 3, 12, 0); // piątek

        var rSun = res(sunEnd.minusHours(1), sunEnd, Reservation.ReservationStatus.COMPLETED);
        rSun.setCheckInTime(null);

        var rFri = res(friEnd.minusHours(1), friEnd, Reservation.ReservationStatus.COMPLETED);
        rFri.setCheckInTime(null);

        when(repo.findAll()).thenReturn(List.of(rSun, rFri));

        var from = LocalDateTime.of(2025, 1, 1, 0, 0);
        var to = LocalDateTime.of(2025, 1, 10, 0, 0);

        var withWeekends = analytics.noShowRate(from, to, false);
        var withoutWeekends = analytics.noShowRate(from, to, true);

        assertThat(withWeekends.denominator()).isEqualTo(2);
        assertThat(withoutWeekends.denominator()).isEqualTo(1);
    }

    @Test
    void utilizationBusinessHours_excludeWeekends_whenFlagTrue() {
        // Okno: piątek 8-18 do poniedziałek 8-18
        var from = LocalDateTime.of(2025, 1, 3, 8, 0); // piątek
        var to = LocalDateTime.of(2025, 1, 5, 18, 0); // niedziela

        // Rezerwacja w sobotę 10-12 (tylko weekend)
        var satStart = LocalDateTime.of(2025, 1, 4, 10, 0);
        var satEnd = LocalDateTime.of(2025, 1, 4, 12, 0);

        var r = res(satStart, satEnd, Reservation.ReservationStatus.ACTIVE);

        when(repo.findAll()).thenReturn(List.of(r));

        var withWeekends = analytics.utilizationBusinessHours(from, to, 8, 18, false);
        var withoutWeekends = analytics.utilizationBusinessHours(from, to,8, 18, true);

        // z weekendami: 2h used, bez weekendów: 0h used
        assertThat(withWeekends.usedMinutes()).isEqualTo(120);
        assertThat(withoutWeekends.usedMinutes()).isEqualTo(0);
    }

    @Test
    void leadTime_avgHours_andWeekendExclusion() {
        var from = LocalDateTime.of(2025, 1, 1, 0, 0);
        var to = LocalDateTime.of(2025, 1, 10, 0, 0);

        // Spotkanie start w piątek 10:00, utworzone 24h wcześniej -> 24h
        var friStart = LocalDateTime.of(2025, 1, 3, 10, 0);
        var rFri = new Reservation();
        rFri.setStartTime(friStart);
        rFri.setCreatedAt(friStart.minusHours(24));

        // Spotkanie start w niedzielę (powinno wypaść przy excludeWeekends)
        var sunStart = LocalDateTime.of(2025, 1, 5, 10, 0);
        var rSun = new Reservation();
        rSun.setStartTime(sunStart);
        rSun.setCreatedAt(sunStart.minusHours(48));

        when(repo.findAll()).thenReturn(List.of(rFri, rSun));

        var withWeekends = analytics.leadTime(from, to, false);
        var withoutWeekends = analytics.leadTime(from, to, true);

        assertThat(withWeekends.sampleSize()).isEqualTo(2);
        assertThat(withoutWeekends.sampleSize()).isEqualTo(1);
        assertThat(withoutWeekends.avgHours()).isEqualTo(24.0);
    }

    @Test
    void peakOccupancy_ignoreWeekendPoints_whenFlagTrue() {
        var from = LocalDateTime.of(2025, 1, 3, 8, 0);
        var to = LocalDateTime.of(2025, 1, 6, 18, 0);

        // Rezerwacja wyłącznie w niedzielę
        var sunStart = LocalDateTime.of(2025, 1, 5, 9, 0);
        var sunEnd = LocalDateTime.of(2025, 1, 5, 10, 0);
        var rSun = res(sunStart, sunEnd, Reservation.ReservationStatus.ACTIVE);

        when(repo.findAll()).thenReturn(List.of(rSun));

        var withWeekends = analytics.peakOccupancy(from, to, false);
        var withoutWeekends = analytics.peakOccupancy(from, to, true);

        assertThat(withWeekends.peak()).isEqualTo(1);
        assertThat(withoutWeekends.peak()).isEqualTo(0);
    }
}
