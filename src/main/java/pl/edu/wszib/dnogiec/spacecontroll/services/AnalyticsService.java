package pl.edu.wszib.dnogiec.spacecontroll.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.wszib.dnogiec.spacecontroll.dao.impl.spring.data.ReservationRepository;
import pl.edu.wszib.dnogiec.spacecontroll.model.Reservation;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private final ReservationRepository reservationRepository;
    private final IConferenceRoomService roomService;

    public record UtilizationResult(long usedMinutes, long availableMinutes, double utilization) {}
    public record RateResult(long numerator, long denominator, double rate) {}
    public record PeakOccupancyResult(int peak, LocalDateTime at) {}
    public record RightSizingResult(double avgDifference, long sampleSize) {}

    public UtilizationResult utilization(LocalDateTime from, LocalDateTime to) {
        List<Reservation> all = reservationRepository.findAll();
        long used = 0;
        for (Reservation r : all) {
            if (r.getStatus() == Reservation.ReservationStatus.CANCELLED) continue;
            LocalDateTime s = r.getStartTime().isBefore(from) ? r.getStartTime() : from;
            LocalDateTime e = r.getEndTime().isBefore(to) ? r.getEndTime() : to;
            if (s.isBefore(e)) used += Duration.between(s, e).toMinutes();
        }
        long rooms = roomService.getAllRooms().size();
        long available = rooms * Math.max(0, Duration.between(from, to).toMinutes());
        double util = available == 0 ? 0.0 : (double) used / available;
        return new UtilizationResult(used, available, util);
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
        record P(LocalDateTime t, int d) {}
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
        int cur = 0, peak = 0; LocalDateTime at = from;
        for (var p : points) { cur += p.d(); if (cur > peak) { peak = cur; at = p.t(); } }
        return new PeakOccupancyResult(peak, at);
    }
}
