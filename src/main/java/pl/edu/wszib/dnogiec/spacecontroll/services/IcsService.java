package pl.edu.wszib.dnogiec.spacecontroll.services;

import org.springframework.stereotype.Service;
import pl.edu.wszib.dnogiec.spacecontroll.model.Reservation;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class IcsService {
    private static final DateTimeFormatter ICS_DT_UTC = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    public byte[] generateReservationIcs(Reservation r, ZoneId tz) {
        String ics = buildCalendarHeader()
                + buildEvent(r, tz)
                + "END:VCALENDAR\r\n";
        return ics.getBytes(StandardCharsets.UTF_8);
    }

    public byte[] generateUserCalendar(String prodId, List<Reservation> reservations, ZoneId tz) {
        StringBuilder sb = new StringBuilder(buildCalendarHeader(prodId));
        for (Reservation r : reservations) {
            sb.append(buildEvent(r,tz));
        }
        sb.append("END:VCALENDAR\r\n");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String buildCalendarHeader() {
        return buildCalendarHeader("-//SpaceControll//EN");
    }

    private String buildCalendarHeader(String prodId) {
        return "BEGIN:VCALENDAR\r\n"
                + "VERSION:2.0\r\n"
                + "PRODID:" + prodId + "\r\n"
                + "CALSCALE:GREGORIAN\r\n";
    }

    private String buildEvent(Reservation r, ZoneId tz) {
        // Interpretujemy LocalDateTime jako czas w strefie tz, a do ICS wypuszczamy UTC (Z)
        ZonedDateTime startZ = r.getStartTime() != null ? r.getStartTime().atZone(tz).withZoneSameInstant(ZoneId.of("UTC")) : null;
        ZonedDateTime endZ = r.getEndTime() != null ? r.getEndTime().atZone(tz).withZoneSameInstant(ZoneId.of("UTC")) : null;

        String uid = "res-" + r.getId() + "@spacecontroll";
        String dtStamp = ZonedDateTime.now(ZoneId.of("UTC")).format(ICS_DT_UTC);
        String dtStart = (startZ != null) ? startZ.format(ICS_DT_UTC) : "";
        String dtEnd = (endZ != null) ? endZ.format(ICS_DT_UTC) : "";

        String roomName = r.getConferenceRoom() != null ? safeText(r.getConferenceRoom().getName()) : "";
        String location = r.getConferenceRoom() != null ? safeText(r.getConferenceRoom().getLocation()) : "";
        String user = r.getUser() != null ? safeText(r.getUser().getLogin()) : "";
        String email = r.getUser() != null ? safeText(r.getUser().getEmail()) : "";
        String notes = r.getNotes() != null ? safeText(r.getNotes()) : "";

        StringBuilder vevent = new StringBuilder();
        vevent.append("BEGIN:VEVENT\r\n");
        vevent.append("UID:").append(uid).append("\r\n");
        vevent.append("DTSTAMP:").append(dtStamp).append("\r\n");
        if (!dtStart.isEmpty()) vevent.append("DTSTART:").append(dtStart).append("\r\n");
        if (!dtEnd.isEmpty()) vevent.append("DTEND:").append(dtEnd).append("\r\n");
        vevent.append("SUMMARY:Rezerwacja sali ").append(roomName).append("\r\n");
        if (!location.isEmpty()) vevent.append("LOCATION:").append(location).append("\r\n");
        if (!notes.isEmpty()) vevent.append("DESCRIPTION:").append(notes).append("\r\n");
        if (!email.isEmpty()) vevent.append("ORGANIZER;CN=").append(user).append(":MAILTO:").append(email).append("\r\n");
        vevent.append("END:VEVENT\r\n");
        return vevent.toString();
    }

    // Ucieczka znaków według iCalendar
    private String safeText(String s) {
        return s.replace("\\", "\\\\")
                .replace("\n", "\\n")
                .replace("\r", "")
                .replace(",", "\\,")
                .replace(";", "\\;");
    }

}
