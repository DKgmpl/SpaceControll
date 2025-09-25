package pl.edu.wszib.dnogiec.spacecontroll.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.test.util.ReflectionTestUtils;
import pl.edu.wszib.dnogiec.spacecontroll.dao.impl.spring.data.ReservationRepository;
import pl.edu.wszib.dnogiec.spacecontroll.model.ConferenceRoom;
import pl.edu.wszib.dnogiec.spacecontroll.model.Reservation;
import pl.edu.wszib.dnogiec.spacecontroll.model.User;
import pl.edu.wszib.dnogiec.spacecontroll.services.impl.ReservationService;

import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {
    @Mock
    ReservationRepository repository;

    @InjectMocks
    ReservationService service;

    private ConferenceRoom room(int cap) {
        ConferenceRoom r = new ConferenceRoom();
        r.setId(1L);
        r.setCapacity(cap);
        r.setName("A");
        r.setLocation("B");
        return r;
    }

    private User user(long id) {
        User u = new User();
        u.setId(id);
        u.setLogin("u" + id);
        return u;
    }

    @BeforeEach
    void initClock() {
        ReflectionTestUtils.setField(service, "clock", Clock.systemDefaultZone());
    }

    @Test
    void isRoomAvailable_detectsOverlap() {
        var start = LocalDateTime.of(2025, 1, 1, 10, 0);
        var end = LocalDateTime.of(2025, 1, 1, 11, 0);

        Reservation existing = new Reservation();
        existing.setStatus(Reservation.ReservationStatus.ACTIVE);
        existing.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 30));
        existing.setEndTime(LocalDateTime.of(2025, 1, 1, 11, 30));

        when(repository.findByConferenceRoomIdAndStatusAndEndTimeAfterAndStartTimeBefore(
                1L, Reservation.ReservationStatus.ACTIVE, start, end
        )).thenReturn(List.of(existing));

        boolean available = service.isRoomAvailable(1L, start, end);
        assertThat(available).isFalse();
    }

    @Test
    void createReservation_rejectsOverCapacity() {
        LocalDateTime start = LocalDate.now().plusDays(1).atTime(10, 0);
        LocalDateTime end = start.plusHours(2);

        Reservation r = new Reservation();
        r.setConferenceRoom(room(5));
        r.setUser(user(1));
        r.setStartTime(start);
        r.setEndTime(end);
        r.setExpectedAttendees(10); // > capacity

        when(repository.findByConferenceRoomIdAndStatusAndEndTimeAfterAndStartTimeBefore(
                anyLong(),
                eq(Reservation.ReservationStatus.ACTIVE),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.of());

        boolean ok = service.createReservation(r);
        assertThat(ok).isFalse();
    }

    @Test
    void cancelReservation_onlyOwnerAndFutureActive() {
        var now = LocalDateTime.now().withSecond(0).withNano(0);

        Reservation r = new Reservation();
        r.setId(100L);
        r.setUser(user(1));
        r.setStatus(Reservation.ReservationStatus.ACTIVE);
        r.setStartTime(now.plusHours(2));
        when(repository.findById(100L)).thenReturn(Optional.of(r));

        boolean ok = service.cancelReservation(100L, 1L);
        assertThat(ok).isTrue();
        assertThat(r.getStatus()).isEqualTo(Reservation.ReservationStatus.CANCELLED);
        assertThat(r.getCancelledAt()).isNotNull();

        //not owner
        when(repository.findById(100L)).thenReturn(Optional.of(r));
        boolean ok2 = service.cancelReservation(100L, 999L);
        assertThat(ok2).isFalse();
    }

    @Test
    void checkIn_onlyWithinWindow() {
        var now = LocalDateTime.now().withSecond(0).withNano(0);

        Reservation r = new Reservation();
        r.setId(200L);
        r.setUser(user(1));
        r.setStatus(Reservation.ReservationStatus.ACTIVE);
        r.setStartTime(now); // window: [-15,+15] min

        when(repository.findById(200L)).thenReturn(Optional.of(r));

        boolean ok = service.checkIn(200L, 1L);
        assertThat(ok).isTrue();
        assertThat(r.getCheckInTime()).isNotNull();
    }
}
