package pl.edu.wszib.dnogiec.spacecontroll.controllers;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pl.edu.wszib.dnogiec.spacecontroll.model.ConferenceRoom;
import pl.edu.wszib.dnogiec.spacecontroll.model.Reservation;
import pl.edu.wszib.dnogiec.spacecontroll.model.User;
import pl.edu.wszib.dnogiec.spacecontroll.services.IConferenceRoomService;
import pl.edu.wszib.dnogiec.spacecontroll.services.IReservationService;
import pl.edu.wszib.dnogiec.spacecontroll.session.SessionConstants;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class ReservationController {
    private final IReservationService reservationService;
    private final IConferenceRoomService conferenceRoomService;

    @GetMapping("/reservations/create/{roomId}")
    public String showReservationForm(@PathVariable Long roomId,
                                      @RequestParam(required = false) String from,
                                      @RequestParam(required = false) String to,
                                      Model model) {
        ConferenceRoom room = conferenceRoomService.getRoomById(roomId);
        Reservation reservation = new Reservation();

        // Obsłuż automatyczne wypełnianie terminów, jeśli przekazano je z rooms.html
        if (from != null && to != null) {
            // Format daty: "yyyy-MM-dd'T'HH:mm"
            try {
                reservation.setStartTime(LocalDateTime.parse(from));
                reservation.setEndTime(LocalDateTime.parse(to));
            } catch (Exception ignored) {
            }
        }
        model.addAttribute("room", room);
        model.addAttribute("reservation", new Reservation());
        return "reservation_create";
    }

    @PostMapping("/reservations/create/{roomId}")
    public String createReservation(@PathVariable Long roomId,
                                    @ModelAttribute("reservation") Reservation reservation,
                                    HttpSession session,
                                    Model model) {

        ConferenceRoom room = conferenceRoomService.getRoomById(roomId);
        User user = (User) session.getAttribute(SessionConstants.USER_KEY);

        reservation.setConferenceRoom(room);
        reservation.setUser(user);

        // Walidacja + próba utworzenia rezerwacji
        boolean success = reservationService.createReservation(reservation);

        if (!success) {
            model.addAttribute("room", room);
            model.addAttribute("reservation", reservation);
            model.addAttribute("error", "Nie można utworzyć rezerwacji (sala jest zajęta lub dane są niepoprawne).");
            return "reservation_create";
        }
        return "redirect:/rooms";
    }

    //Moje rezerwacje - widok listy rezerwacji użytkownika
    @GetMapping("/reservations/my")
    public String showMyReservations(HttpSession session, Model model) {
        User user = (User) session.getAttribute(SessionConstants.USER_KEY);
        model.addAttribute("myReservations", reservationService.getReservationsForUser(user.getId()));
        return "my_reservations";
    }

    //Anulowanie rezerwacji
    @PostMapping("/reservations/cancel/{reservationId}")
    public String cancelReservation(@PathVariable Long reservationId, HttpSession session) {
        User user = (User) session.getAttribute(SessionConstants.USER_KEY);
        reservationService.cancelReservation(reservationId, user.getId());
        return "redirect:/reservations/my";
    }
}
