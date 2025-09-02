package pl.edu.wszib.dnogiec.spacecontroll.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.wszib.dnogiec.spacecontroll.dao.impl.spring.data.ReservationRepository;
import pl.edu.wszib.dnogiec.spacecontroll.model.Reservation;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private final ReservationRepository reservationRepository;
    private final IConferenceRoomService roomService;

    public record UtilizationResult(long usedMinutes, long availableMinutes, double utilization) {
    }
    public record RateResult(long numerator, long denominator, double rate) {
    }
    public record PeakOccupancyResult(int peak, LocalDateTime at) {
    }
    public record RightSizingResult(double avgDifference, long sampleSize) {
    }

    // Liczenie gdzie godzin jest 24/7 dni.
    /*public UtilizationResult utilization(LocalDateTime from, LocalDateTime to) {
        // 1) Rezerwacje, które przecinają okno [from,to]
        var intersecting = reservationRepository.findAll().stream()
                .filter(r -> r.getStatus() != Reservation.ReservationStatus.CANCELLED)
                .filter(r -> r.getStartTime().isBefore(to) && r.getEndTime().isAfter(from))
                .toList();
        // 2) Suma czasu przecięcia z oknem
        long used = 0;
        for (var r : intersecting) {
            var s = r.getStartTime().isAfter(from) ? r.getStartTime() : from;
            var e = r.getEndTime().isBefore(to) ? r.getEndTime() : to;
            if (s.isBefore(e)) {
                used += java.time.Duration.between(s, e).toMinutes();
            }
        }
        // 3) Dostępne minuty = liczba sal * minuty w oknie
        long rooms = roomService.getAllRooms().size();
        long window = Math.max(0, java.time.Duration.between(from, to).toMinutes());
        long available = rooms * window;
        double util = available == 0 ? 0.0 : (double) used / available;
        return new UtilizationResult(used, available, util);
    }*/

    // Liczenie w godzinach pracy (8-18) (zarówno used, jak i available)
    public UtilizationResult utilizationBusinessHours(LocalDateTime from, LocalDateTime to, int startHour, int endHour) {
        validateHours(startHour, endHour);
        var intersecting = reservationRepository.findAll().stream()
                .filter(r -> r.getStatus() != Reservation.ReservationStatus.CANCELLED)
                .filter(r -> r.getStartTime().isBefore(to) && r.getEndTime().isAfter(from))
                .toList();

        long usedBusiness = 0;
        for (var r : intersecting) {
            usedBusiness += overlapWithBusinessWindows(r.getStartTime(), r.getEndTime(), from, to, startHour, endHour);
        }

        long rooms = roomService.getAllRooms().size();
        long available = rooms * businessMinutes(from, to, startHour, endHour);
        double util = available == 0 ? 0.0 : (double) usedBusiness / available;
        return new UtilizationResult(usedBusiness, available, util);
    }

    public RateResult noShowRate(LocalDateTime from, LocalDateTime to) {
        var ended = reservationRepository.findAll().stream()
                .filter(r -> r.getEndTime().isAfter(from) && r.getEndTime().isBefore(to))
                .filter(r -> r.getStatus() == Reservation.ReservationStatus.COMPLETED)
                .toList();
        long total = ended.size();
        long noshow = ended.stream().filter(r -> r.getCheckInTime() == null).count();
        return new RateResult(noshow, total, total == 0 ? 0.0 : (double) noshow / total);
    }

    public RateResult cancellationRate(LocalDateTime from, LocalDateTime to) {
        var created = reservationRepository.findAll().stream()
                .filter(r -> r.getCreatedAt() != null && !r.getCreatedAt().isBefore(from) && !r.getCreatedAt().isAfter(to))
                .toList();
        long total = created.size();
        long cancelled = created.stream().filter(r -> r.getStatus() == Reservation.ReservationStatus.CANCELLED).count();
        return new RateResult(cancelled, total, total == 0 ? 0.0 : (double) cancelled / total);
    }

    public RightSizingResult rightSizing(LocalDateTime from, LocalDateTime to) {
        var attended = reservationRepository.findAll().stream()
                .filter(r -> r.getCheckInTime() != null)
                .filter(r -> r.getStartTime().isAfter(from) && r.getEndTime().isBefore(to))
                .filter(r -> r.getExpectedAttendees() != null && r.getConferenceRoom() != null)
                .toList();
        long n = attended.size();
        double avgDiff = n == 0 ? 0.0 :
                attended.stream().mapToInt(r -> r.getConferenceRoom().getCapacity() - r.getExpectedAttendees()).average().orElse(0.0);
        return new RightSizingResult(avgDiff, n);
    }

    public PeakOccupancyResult peakOccupancy(LocalDateTime from, LocalDateTime to) {
        record P(LocalDateTime t, int d) {
        }
        var points = reservationRepository.findAll().stream()
                .filter(r -> r.getStatus() != Reservation.ReservationStatus.CANCELLED)
                .map(r -> {
                    LocalDateTime s = r.getStartTime().isBefore(from) ? from : r.getStartTime();
                    LocalDateTime e = r.getEndTime().isAfter(to) ? to : r.getEndTime();
                    return List.of(new P(s, +1), new P(e, -1));
                })
                .flatMap(List::stream)
                .sorted((a, b) -> a.t().compareTo(b.t()))
                .toList();
        int cur = 0, peak = 0;
        LocalDateTime at = from;
        for (var p : points) {
            cur += p.d();
            if (cur > peak) {
                peak = cur;
                at = p.t();
            }
        }
        return new PeakOccupancyResult(peak, at);
    }

    private static long businessMinutes(LocalDateTime from, LocalDateTime to, int startHour, int endHour) {
        long total = 0;
        for (var d = from.toLocalDate(); !d.isAfter(to.toLocalDate()); d = d.plusDays(1)) {
            var dayStart = d.atTime(startHour, 0);
            var dayEnd = d.atTime(endHour, 0);
            var s = max(dayStart, from);
            var e = min(dayEnd, to);
            if (s.isBefore(e)) {
                total += java.time.Duration.between(s, e).toMinutes();
            }
        }
        return Math.max(0, total);
    }

    private static void validateHours(int start, int end) {
        if (start < 0 || start >= 24 || end <= start || end > 24)
            throw new IllegalArgumentException("Nieprawidłowe godziny pracy: " + start + "-" + end);
    }


    private static long overlapWithBusinessWindows(LocalDateTime rs, LocalDateTime re,
                                                   LocalDateTime from, LocalDateTime to,
                                                   int startHour, int endHour) {
        long total = 0;
        // iteruj po dniach w oknie [from,to]
        for (var d = from.toLocalDate(); !d.isAfter(to.toLocalDate()); d = d.plusDays(1)) {
            var dayStart = d.atTime(startHour, 0);
            var dayEnd = d.atTime(endHour, 0);
            // przecięcie trzech odcinków: rezerwacji [rs,re], okna globalnego [from,to] i okna dnia [dayStart,dayEnd]
            var s = max(max(rs, from), dayStart);
            var e = min(min(re, to), dayEnd);
            if (s.isBefore(e)) {
                total += java.time.Duration.between(s, e).toMinutes();
            }
        }
        return Math.max(0, total);
    }

    private static LocalDateTime max(LocalDateTime a, LocalDateTime b) {
        return a.isAfter(b) ? a : b;
    }

    private static LocalDateTime min(LocalDateTime a, LocalDateTime b) {
        return a.isBefore(b) ? a : b;
    }
}