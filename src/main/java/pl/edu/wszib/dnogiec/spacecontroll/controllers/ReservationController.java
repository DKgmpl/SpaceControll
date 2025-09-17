package pl.edu.wszib.dnogiec.spacecontroll.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.edu.wszib.dnogiec.spacecontroll.dao.impl.spring.data.UserRepository;
import pl.edu.wszib.dnogiec.spacecontroll.model.ConferenceRoom;
import pl.edu.wszib.dnogiec.spacecontroll.model.Reservation;
import pl.edu.wszib.dnogiec.spacecontroll.model.User;
import pl.edu.wszib.dnogiec.spacecontroll.services.IConferenceRoomService;
import pl.edu.wszib.dnogiec.spacecontroll.services.IReservationService;
import pl.edu.wszib.dnogiec.spacecontroll.validation.ValidationGroups;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ReservationController {
    private final IReservationService reservationService;
    private final IConferenceRoomService conferenceRoomService;
    private final UserRepository userRepository;

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
        model.addAttribute("reservation", reservation);
        return "reservation_create";
    }

    @PostMapping("/reservations/create/{roomId}")
    public String createReservation(@PathVariable Long roomId,
                                    @Validated(ValidationGroups.WebChecks.class) @ModelAttribute("reservation") Reservation reservation,
                                    org.springframework.validation.BindingResult bindingResult,
                                    Model model) {

        ConferenceRoom room = conferenceRoomService.getRoomById(roomId);
        User user = getCurrentUser();

        reservation.setConferenceRoom(room);
        reservation.setUser(user);

        // Walidacja formularza (JSR-380)
        if (bindingResult.hasErrors()) {
            model.addAttribute("room", room);
            return "reservation_create";
        }

        // Logika biznesowa: dostępność sal + próba utworzenia rezerwacji
        boolean success = reservationService.createReservation(reservation);

        if (!success) {
            model.addAttribute("room", room);
            model.addAttribute("reservation", reservation);
            model.addAttribute("error", "Nie można utworzyć rezerwacji (sala jest zajęta lub dane są niepoprawne).");
            return "reservation_create";
        }
        return "redirect:/rooms";
    }

    @GetMapping("/reservations/export")
    public ResponseEntity<byte[]> exportReservationsCsv(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Reservation.ReservationStatus status){
        List<Reservation> list = reservationService.getAllReservations();

        // Filtry (in-memory)
        if (from != null) list = list.stream().filter(r -> !r.getEndTime().isBefore(from)).toList();
        if (to != null) list = list.stream().filter(r -> !r.getStartTime().isAfter(to)).toList();
        if (roomId != null) list = list.stream().filter(r -> r.getConferenceRoom() != null && roomId.equals(r.getConferenceRoom().getId())).toList();
        if (status != null) list = list.stream().filter(r -> r.getStatus() == status).toList();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        StringBuilder sb = new StringBuilder();
        sb.append("id;room;location;capacity;user;start;end;status;expectedAttendees;checkInTime;createdAt;cancelledAt;notes\n");
        for (Reservation r : list) {
            sb.append(r.getId()).append(';')
                    .append(s(r.getConferenceRoom() != null ? r.getConferenceRoom().getName() : "")).append(';')
                    .append(s(r.getConferenceRoom() != null ? r.getConferenceRoom().getLocation() : "")).append(';')
                    .append(r.getConferenceRoom() != null ? r.getConferenceRoom().getCapacity() : "").append(';')
                    .append(s(r.getUser() != null ? r.getUser().getLogin() : "")).append(';')
                    .append(r.getStartTime() != null ? fmt.format(r.getStartTime()) : "").append(';')
                    .append(r.getEndTime() != null ? fmt.format(r.getEndTime()) : "").append(';')
                    .append(r.getStatus() != null ? r.getStatus().name() : "").append(';')
                    .append(r.getExpectedAttendees() != null ? r.getExpectedAttendees() : "").append(';')
                    .append(r.getCheckInTime() != null ? fmt.format(r.getCheckInTime()) : "").append(';')
                    .append(r.getCreatedAt() != null ? fmt.format(r.getCreatedAt()) : "").append(';')
                    .append(r.getCancelledAt() != null ? fmt.format(r.getCancelledAt()) : "").append(';')
                    .append(s(r.getNotes() != null ? r.getNotes() : "")).append('\n');
        }

        byte[] bom = new byte[] {(byte)0xEF,(byte)0xBB,(byte)0xBF};
        byte[] body = (new String(bom, StandardCharsets.UTF_8) + sb).getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reservations.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    //Moje rezerwacje - widok listy rezerwacji użytkownika
    @GetMapping("/reservations/my")
    public String showMyReservations(Model model) {
        User user = getCurrentUser();
        model.addAttribute("myReservations", reservationService.getReservationsForUser(user.getId()));
        return "my_reservations";
    }

    //Anulowanie rezerwacji
    @PostMapping("/reservations/cancel/{reservationId}")
    public String cancelReservation(@PathVariable Long reservationId) {
        User user = getCurrentUser();
        reservationService.cancelReservation(reservationId, user.getId());
        return "redirect:/reservations/my";
    }

    @PostMapping("/reservations/checkIn/{reservationId}")
    public String checkIn(@PathVariable Long reservationId) {
        User user = getCurrentUser();
        reservationService.checkIn(reservationId, user.getId());
        return "redirect:/reservations/my";
    }

    //Metoda pomocnicza do pobrania aktualnie zautoryzowanego użytkownika
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        return userRepository.findByLogin(currentUsername)
                .orElseThrow(() -> new RuntimeException("Użytkownik nie znaleziony: " + currentUsername));
    }

    //Pomocnik do escapingu CSV
    private static String s(String v) {
        if (v == null) return "";
        String escaped = v.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
