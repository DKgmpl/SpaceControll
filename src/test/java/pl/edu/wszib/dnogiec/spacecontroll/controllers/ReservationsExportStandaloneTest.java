package pl.edu.wszib.dnogiec.spacecontroll.controllers;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(MockitoExtension.class)
class ReservationsExportStandaloneTest {
    @Mock
    IReservationService reservationService;
    @Mock
    IConferenceRoomService conferenceRoomService;
    @Mock
    UserRepository userRepository;
    @Mock
    IcsService icsService;

    MockMvc mvc;

    @BeforeEach
    void setup() {
        var controller = new ReservationController(reservationService, conferenceRoomService, userRepository, icsService);
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private Reservation sample() {
        var room = new ConferenceRoom();
        room.setName("A101"); room.setLocation("Budynek A"); room.setCapacity(6);

        var u = new User();
        u.setLogin("user");

        var r = new Reservation();
        r.setId(1L);
        r.setConferenceRoom(room);
        r.setUser(u);
        r.setStartTime(LocalDateTime.of(2025,1,1,10,0));
        r.setEndTime(LocalDateTime.of(2025,1,1,11,0));
        r.setStatus(Reservation.ReservationStatus.ACTIVE);
        r.setExpectedAttendees(4);
        return r;
    }

    @Test
    void exportReservationCsv_ok() throws Exception {
        when(reservationService.getAllReservations()).thenReturn(List.of(sample()));

        ResultActions res = mvc.perform(get("/reservations/export")
                .param("from", "2025-01-01T00:00")
                .param("to", "2025-01-01T23:59"));

        res.andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("reservations.csv")))
            .andExpect(content().contentType("text/csv;charset=UTF-8"))
            .andExpect(content().string(containsString("A101")))
            .andExpect(content().string(containsString("ACTIVE")));
    }
}
