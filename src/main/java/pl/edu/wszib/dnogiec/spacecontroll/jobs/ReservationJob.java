package pl.edu.wszib.dnogiec.spacecontroll.jobs;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.edu.wszib.dnogiec.spacecontroll.dao.impl.spring.data.ReservationRepository;
import pl.edu.wszib.dnogiec.spacecontroll.model.Reservation;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservationJob {
    private final ReservationRepository reservationRepository;

    @Scheduled(fixedRate = 60_000)
    public void completeFinished() {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> active = reservationRepository.findByStatus(Reservation.ReservationStatus.ACTIVE);
        for (Reservation r : active) {
            if (r.getEndTime().isBefore(now)) {
                r.setStatus(Reservation.ReservationStatus.COMPLETED);
                reservationRepository.save(r);
            }
        }
    }
}
