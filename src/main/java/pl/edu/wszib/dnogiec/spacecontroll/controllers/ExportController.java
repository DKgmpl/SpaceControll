package pl.edu.wszib.dnogiec.spacecontroll.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pl.edu.wszib.dnogiec.spacecontroll.model.Reservation;
import pl.edu.wszib.dnogiec.spacecontroll.services.IConferenceRoomService;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class ExportController {
    private final IConferenceRoomService roomService;

    @GetMapping("/exports")
    public String exportPage(Model model) {
        // Domyślne zakresy: ostatnie 7 dni (00:00 – 23:59)
        LocalDate today = LocalDate.now();
        LocalDateTime defFrom = today.minusDays(6).atStartOfDay().withSecond(0).withNano(0);
        LocalDateTime defTo = today.atTime(23, 59).withSecond(0).withNano(0);

        model.addAttribute("from", defFrom);
        model.addAttribute("to", defTo);
        model.addAttribute("rooms", roomService.getAllRooms());
        model.addAttribute("statuses", Reservation.ReservationStatus.values());

        return "exports";
    }
}
