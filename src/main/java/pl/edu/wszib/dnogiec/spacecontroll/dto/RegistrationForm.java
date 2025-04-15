package pl.edu.wszib.dnogiec.spacecontroll.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistrationForm {
    @NotBlank(message = "Email nie może być pusty")
    @Email(message = "Podaj poprawny adres email")
    private String email;

    @NotBlank(message = "Login nie może być pusty")
    @Size(min = 3, max = 20, message = "Login musi mieć od 3 do 20 znaków")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "Login może zawierać jedynie litery, cyfry, _ , . lub -")
    private String login;

    @NotBlank(message = "Hasło nie może być puste")
    @Size(min = 8, message = "Hasło musi zawierać co najmniej 8 znaków")
    private String password;

    @NotBlank(message = "Imię nie może być puste")
    @Size(min = 2, max = 30, message = "Imię musi mieć od 2 do 30 znaków")
    @Pattern(regexp = "^[A-Za-ząćęłńóśźżĄĆĘŁŃÓŚŹŻ]+$", message = "Imię może zawierać tylko litery")
    private String name;

    @NotBlank(message = "Nazwisko nie może być puste")
    @Size(min = 2, max = 30, message = "Nazwisko musi mieć od 2 do 30 znaków")
    @Pattern(regexp = "^[A-Za-ząćęłńóśźżĄĆĘŁŃÓŚŹŻ]+$", message = "Nazwisko może zawierać tylko litery")
    private String surname;
}
