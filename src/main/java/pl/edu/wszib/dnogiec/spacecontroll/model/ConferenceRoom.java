package pl.edu.wszib.dnogiec.spacecontroll.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "app_conference_room")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ConferenceRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String location;
    private int capacity;
    private String equipment;
    private String description;

    @OneToMany(mappedBy = "conferenceRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations;
}
