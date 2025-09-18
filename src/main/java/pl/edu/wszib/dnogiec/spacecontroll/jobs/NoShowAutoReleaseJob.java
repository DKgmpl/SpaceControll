package pl.edu.wszib.dnogiec.spacecontroll.jobs;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.edu.wszib.dnogiec.spacecontroll.dao.impl.spring.data.ReservationRepository;
import pl.edu.wszib.dnogiec.spacecontroll.model.Reservation;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NoShowAutoReleaseJob {
    private final ReservationRepository reservationRepository;

    @Value("${app.no-show.grace-minutes:15}")
    private int graceMinutes;

    //co minutę zwalniaj rezerwacje bez check-in po upływie graceMinutes od startu
    @Scheduled(fixedRate = 60_000)
    public void autoRelease() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(graceMinutes);

        List<Reservation> candidates =
                reservationRepository.findByStatusAndStartTimeBeforeAndCheckInTimeIsNull(
                        Reservation.ReservationStatus.ACTIVE, threshold);

        if (!candidates.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            for (Reservation r : candidates) {
                r.setStatus(Reservation.ReservationStatus.NO_SHOW_RELEASED);
                r.setCheckInTime(now);
                reservationRepository.save(r);
            }
            System.out.println("[AutoRelease] Released " + candidates.size() + " reservations as NO_SHOW_RELEASED (grace=" + graceMinutes + "m)");
        }
    }
}
