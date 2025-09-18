package pl.edu.wszib.dnogiec.spacecontroll.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import pl.edu.wszib.dnogiec.spacecontroll.validation.EndAfterStart;
import pl.edu.wszib.dnogiec.spacecontroll.validation.MaxDurationHours;
import pl.edu.wszib.dnogiec.spacecontroll.validation.SameDay;
import pl.edu.wszib.dnogiec.spacecontroll.validation.ValidationGroups;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_reservation")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EndAfterStart
@SameDay
@MaxDurationHours(value = 12)
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "conference_room_id")   //, referencedColumnName = "id"
    private ConferenceRoom conferenceRoom;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")   //, referencedColumnName = "id"
    private User user;

    @NotNull(message = "Data i godzina rozpoczęcia są wymagane.")
    @FutureOrPresent(message = "Data rozpoczęcia nie może być w przeszłości.",
            groups = ValidationGroups.WebChecks.class)
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startTime;

    @NotNull(message = "Data i godzina zakończenia są wymagane")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endTime;

    // Status rezerwacji; przykładowe wartości: ACTIVE - aktywna, CANCELLED - anulowana, COMPLETED - zakończona
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private String notes;

    private Integer expectedAttendees;
    private LocalDateTime checkInTime;
    private LocalDateTime createdAt;
    private LocalDateTime cancelledAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public enum ReservationStatus {
        ACTIVE,
        CANCELLED,
        COMPLETED,
        NO_SHOW_RELEASED    // auto-zwolnienie bez check-in
    }
}
