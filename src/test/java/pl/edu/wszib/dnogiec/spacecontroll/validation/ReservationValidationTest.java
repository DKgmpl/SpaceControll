package pl.edu.wszib.dnogiec.spacecontroll.validation;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pl.edu.wszib.dnogiec.spacecontroll.model.Reservation;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class ReservationValidationTest {
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private Reservation res(LocalDateTime start, LocalDateTime end) {
        Reservation r = new Reservation();
        r.setStartTime(start);
        r.setEndTime(end);
        return r;
    }

    @Test
    void endMustBeAfterStart() {
        var now = LocalDateTime.now().withSecond(0).withNano(0);
        var r = res(now, now);
        var v = validator.validate(r);
        assertThat(v).anyMatch(cv -> cv.getMessage().contains("zakończenia") || cv.getMessage().contains("po czasie"));
    }

    @Test
    void mustBeSameDay() {
        var start = LocalDateTime.of(2025, 1, 1, 10, 0);
        var end = LocalDateTime.of(2025, 1, 2, 9, 0);
        var v = validator.validate(res(start, end));
        assertThat(v).anyMatch(cv -> cv.getMessage().toLowerCase().contains("jednego dnia"));
    }

    @Test
    void maxDuration12h() {
        var start = LocalDateTime.of(2025, 1, 1, 8, 0);
        var end = start.plusHours(13);
        var r = res(start, end);
        var v = validator.validate(r);
        assertThat(v).anyMatch(cv -> cv.getMessage().toLowerCase().contains("przekracza"));
    }

    @Test
    void okCase() {
        var start = LocalDateTime.of(2025, 1, 1, 8, 0);
        var end = LocalDateTime.of(2025, 1, 1, 10, 0);
        var r = res(start, end);
        var v = validator.validate(r);
        assertThat(v).isEmpty();
    }
}
