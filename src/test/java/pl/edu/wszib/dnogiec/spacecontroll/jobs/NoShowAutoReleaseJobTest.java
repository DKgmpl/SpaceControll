package pl.edu.wszib.dnogiec.spacecontroll.jobs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.wszib.dnogiec.spacecontroll.dao.impl.spring.data.ReservationRepository;
import pl.edu.wszib.dnogiec.spacecontroll.model.Reservation;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoShowAutoReleaseJobTest {

    @Mock
    ReservationRepository repo;

    @InjectMocks
    NoShowAutoReleaseJob job;

    @Test
    void autoRelease_marksCandidatesAndSaves() throws Exception {
        // ustaw graceMinutes=15 przez refleksję
        var f = NoShowAutoReleaseJob.class.getDeclaredField("graceMinutes");
        f.setAccessible(true);
        f.setInt(job, 15);

        var now = LocalDateTime.now().withSecond(0).withNano(0);
        var r1 = new Reservation();
        r1.setId(1L);
        r1.setStatus(Reservation.ReservationStatus.ACTIVE);
        r1.setStartTime(now.minusMinutes(20)); //po progu
        r1.setCheckInTime(null);

        when(repo.findByStatusAndStartTimeBeforeAndCheckInTimeIsNull(
                eq(Reservation.ReservationStatus.ACTIVE), any(LocalDateTime.class)
        )).thenReturn(List.of(r1));

        job.autoRelease();

        assertThat(r1.getStatus()).isEqualTo(Reservation.ReservationStatus.NO_SHOW_RELEASED);
        assertThat(r1.getCancelledAt()).isNotNull();
        verify(repo, atLeastOnce()).save(r1);
    }
}
