package pl.edu.wszib.dnogiec.spacecontroll.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "app_user")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String surname;
    private String login;
    private String password;
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    public User(Long id, String name, String surname, String login, String password, Role role) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.login = login;
        this.password = password;
        this.role = role;
    }

    public User(Long id) {
        this.id = id;
    }

    public enum Role {
        USER,
        ADMIN
    }
}
