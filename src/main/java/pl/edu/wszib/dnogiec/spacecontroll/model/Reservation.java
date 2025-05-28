package pl.edu.wszib.dnogiec.spacecontroll.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_reservation")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
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

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Status rezerwacji; przykładowe wartości: ACTIVE - aktywna, CANCELLED - anulowana, COMPLETED - zakończona
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private String notes;

    public enum ReservationStatus {
        ACTIVE,
        CANCELLED,
        COMPLETED
    }
}
