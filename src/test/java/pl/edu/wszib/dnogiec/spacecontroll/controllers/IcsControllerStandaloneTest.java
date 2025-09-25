package pl.edu.wszib.dnogiec.spacecontroll.controllers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pl.edu.wszib.dnogiec.spacecontroll.dao.impl.spring.data.UserRepository;
import pl.edu.wszib.dnogiec.spacecontroll.model.ConferenceRoom;
import pl.edu.wszib.dnogiec.spacecontroll.model.Reservation;
import pl.edu.wszib.dnogiec.spacecontroll.model.User;
import pl.edu.wszib.dnogiec.spacecontroll.services.IConferenceRoomService;
import pl.edu.wszib.dnogiec.spacecontroll.services.IReservationService;
import pl.edu.wszib.dnogiec.spacecontroll.services.IcsService;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class IcsControllerStandaloneTest {
    @Mock
    IReservationService reservationService;
    @Mock
    IConferenceRoomService conferenceRoomService;
    @Mock
    UserRepository userRepository;

    IcsService icsService = new IcsService();

    MockMvc mvc;
    ReservationController controller;

    @BeforeEach
    void setUp() throws Exception {
        controller = new ReservationController(reservationService, conferenceRoomService, userRepository, icsService);
        // Strefa czasowa dla ICS
        setField(controller, "icsTimezone", "Europe/Warsaw");
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static void setAuth(String username) {
        var auth = new UsernamePasswordAuthenticationToken(username, "N/A");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private ConferenceRoom room(String name, String location, int cap) {
        var r = new ConferenceRoom();
        r.setName(name);
        r.setLocation(location);
        r.setCapacity(cap);
        return r;
    }

    private User adminUser() {
        var u = new User();
        u.setId(999L);
        u.setLogin("admin");
        u.setRole(User.Role.ADMIN);
        u.setEmail("admin@example.com");
        return u;
    }

    private User user(long id, String login, User.Role role) {
        var u = new User();
        u.setId(id);
        u.setLogin(login);
        u.setRole(role);
        u.setEmail(login + "@example.com");
        return u;
    }

    @Test
    void reservationIcs_asAdmin_returnIcsFile() throws Exception {
        // auth jako admin
        setAuth("admin");
        when(userRepository.findByLogin(eq("admin")))
                .thenReturn(Optional.of(user(999L, "admin", User.Role.ADMIN)));

        // przykładowa rezerwacja
        var r = new Reservation();
        r.setId(42L);
        r.setUser(user(1L, "u1", User.Role.USER));
        r.setConferenceRoom(room("A101", "Budynek A", 6));
        r.setStartTime(LocalDateTime.of(2025, 1, 15, 10, 0));
        r.setEndTime(LocalDateTime.of(2025, 1, 15, 11, 0));
        r.setStatus(Reservation.ReservationStatus.ACTIVE);

        when(reservationService.getReservationById(anyLong())).thenReturn(r);

        ResultActions res = mvc.perform(get("/reservations/42/ics"));

        res.andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("reservation-42.ics")))
                .andExpect(content().contentType("text/calendar;charset=UTF-8"))
                .andExpect(content().string(containsString("BEGIN:VCALENDAR")))
                .andExpect(content().string(containsString("BEGIN:VEVENT")))
                .andExpect(content().string(containsString("DTSTART")))
                .andExpect(content().string(containsString("DTEND")))
                .andExpect(content().string(containsString("SUMMARY:Rezerwacja sali A101")));
    }

    @Test
    void myCalendarIcs_asUser_filtersCancelledAndReturnsFeed() throws Exception {
        // auth jako użytkownik
        setAuth("user");
        when(userRepository.findByLogin(eq("user")))
                .thenReturn(Optional.of(user(1L, "user", User.Role.USER)));

        // Rezerwacje: jedna aktywna w oknie, jedna anulowana (powinna zostać odfiltrowana)
        var active = new Reservation();
        active.setId(1L);
        active.setUser(user(1L, "user", User.Role.USER));
        active.setConferenceRoom(room("B201", "Budynek B", 8));
        active.setStartTime(LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0));
        active.setEndTime(active.getStartTime().plusHours(1));
        active.setStatus(Reservation.ReservationStatus.ACTIVE);

        var cancelled = new Reservation();
        cancelled.setId(2L);
        cancelled.setUser(user(1L, "user", User.Role.USER));
        cancelled.setConferenceRoom(room("C301", "Budynek C", 10));
        cancelled.setStartTime(LocalDateTime.now().plusDays(2).withHour(12).withMinute(0).withSecond(0).withNano(0));
        cancelled.setEndTime(cancelled.getStartTime().plusHours(1));
        cancelled.setStatus(Reservation.ReservationStatus.CANCELLED);

        when(reservationService.getReservationsForUser(1L))
                .thenReturn(List.of(active, cancelled));

        ResultActions res = mvc.perform(get("/reservations/my.ics"));

        res.andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("my-reservation.ics")))
                .andExpect(content().contentType("text/calendar;charset=UTF-8"))
                .andExpect(content().string(containsString("BEGIN:VCALENDAR")))
                .andExpect(content().string(containsString("BEGIN:VEVENT")))
                .andExpect(content().string(containsString("SUMMARY:Rezerwacja sali B201")))
                .andExpect(content().string(containsString("DTSTART")))
                .andExpect(content().string(containsString("DTEND")));
    }
}
