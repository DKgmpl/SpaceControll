package pl.edu.wszib.dnogiec.spacecontroll.services.impl;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.edu.wszib.dnogiec.spacecontroll.dao.impl.spring.data.UserRepository;
import pl.edu.wszib.dnogiec.spacecontroll.exceptions.RegisterValidationExemption;
import pl.edu.wszib.dnogiec.spacecontroll.model.User;
import pl.edu.wszib.dnogiec.spacecontroll.services.IAuthenticationService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements IAuthenticationService {

    private final UserRepository userRepository;
    private final HttpSession httpSession;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void register(String email, String login, String password, String name, String surname) {
        String normLogin = login.trim().toLowerCase(java.util.Locale.ROOT);
        String normEmail = email.trim().toLowerCase(java.util.Locale.ROOT);

        if (userRepository.findByLogin(normLogin).isPresent()) {
            throw new RegisterValidationExemption("Użytkownik o podanym loginie już istnieje");
        }
        if (userRepository.findByEmail(normEmail).isPresent()) {
            throw new RegisterValidationExemption("Użytkownik o podanym adresie email już istnieje");
        }

        User newUser = new User();
        newUser.setLogin(normLogin);
        newUser.setPassword(passwordEncoder.encode(password)); // Use Spring Security's password encoder
        newUser.setEmail(normEmail);
        newUser.setName(name);
        newUser.setSurname(surname);
        newUser.setRole(User.Role.USER);
        this.userRepository.save(newUser);
        System.out.println("Zarejestrowano nowego użytkownika");
    }

    @Override
    public void showAllData() {
        Iterable<User> allUsers = this.userRepository.findAll();
        allUsers.forEach(System.out::println);
    }

    @Override
    public String getLoginInfo() {
        String temp = (String) this.httpSession.getAttribute("loginInfo");
        this.httpSession.removeAttribute("loginInfo");
        return temp;
    }

    @Override
    public String getRegisterInfo() {
        String temp = (String) this.httpSession.getAttribute("registerInfo");
        this.httpSession.removeAttribute("registerInfo");
        return temp;
    }
}
