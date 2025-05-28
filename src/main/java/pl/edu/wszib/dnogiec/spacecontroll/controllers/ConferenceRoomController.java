package pl.edu.wszib.dnogiec.spacecontroll.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.edu.wszib.dnogiec.spacecontroll.model.ConferenceRoom;
import pl.edu.wszib.dnogiec.spacecontroll.services.IConferenceRoomService;
import pl.edu.wszib.dnogiec.spacecontroll.services.IReservationService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ConferenceRoomController {

    private final IConferenceRoomService conferenceRoomService;
    private final IReservationService reservationService;

    // Lista sal konferencyjnych z formularzem wyboru przedziału czasu
    @GetMapping("/rooms")
    public String listRooms(
            Model model,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to
    ) {
        // Pobierz sale
        List<ConferenceRoom> rooms = conferenceRoomService.getAllRooms();

        // Ustal domyślny zakres [from,to] jeśli brak w zapytaniu
        LocalDateTime searchFrom = (from != null) ? from : LocalDateTime.now().plusMinutes(1);
        LocalDateTime searchTo = (to != null) ? to : LocalDateTime.now().plusHours(1);

        // Sformatowane Stringi do field datetime-local
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        String fromString = searchFrom.format(formatter);
        String toString = searchTo.format(formatter);

        // Mapa dostępności sal (czy wolna w zadanym zakresie)
        Map<Long, Boolean> availabilityMap = new HashMap<>();
        for (ConferenceRoom room : rooms) {
            boolean available = reservationService.isRoomAvailable(room.getId(), searchFrom, searchTo);
            availabilityMap.put(room.getId(), available);
        }

        model.addAttribute("rooms", rooms);
        model.addAttribute("availabilityMap", availabilityMap);
        model.addAttribute("fromString", fromString);
        model.addAttribute("toString", toString);

        return "rooms";
    }

    // Formularz dodawania sal konferencyjnych - Tylko dla admina
    @GetMapping("/rooms/add")
    public String showAddRoomForm(Model model) {
        model.addAttribute("room", new ConferenceRoom());
        return "/room_add";
    }

    // Obsługa zapisu nowej sali kofnerencyjnej - Tylko dla admina
    @PostMapping("/rooms/add")
    public String addRoom(@ModelAttribute("room") ConferenceRoom room) {
        conferenceRoomService.save(room);
        return "redirect:/rooms";
    }
}
