package pl.edu.wszib.dnogiec.spacecontroll.services.impl;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import pl.edu.wszib.dnogiec.spacecontroll.dao.impl.spring.data.UserRepository;
import pl.edu.wszib.dnogiec.spacecontroll.model.User;
import pl.edu.wszib.dnogiec.spacecontroll.services.IAuthenticationService;
import pl.edu.wszib.dnogiec.spacecontroll.session.SessionConstants;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements IAuthenticationService {

    private final UserRepository userRepository;
    private final HttpSession httpSession;

    @Override
    public void login(String login, String password) {
        Optional<User> user = this.userRepository.findByLogin(login);
        if (user.isPresent() &&
                DigestUtils.md5DigestAsHex(password.getBytes()).equals(user.get().getPassword())) {
            httpSession.setAttribute(SessionConstants.USER_KEY, user.get());
            //Miejsce na resztę Atrybutów SessionConstants
            System.out.println("Zalogowano");
            return;
        }
        this.httpSession.setAttribute("loginInfo", "Nieprawidłowy login lub hasło");
        System.out.println("Błąd logowania - Nieprawidłowy login lub hasło");

    }

    @Override
    public void register(String email, String login, String password, String name, String surname) {
        Optional<User> existingUser = this.userRepository.findByLogin(login);
        if (existingUser.isPresent()) {
            this.httpSession.setAttribute("registerInfo", "Użytkownik o podanym loginie już istnieje");
            System.out.println("Błąd rejestracji nowego użytkownika - Użytkownik o podanym loginie już istnieje");
            return; // Przerwij dalsze przetwarzanie, aby uniknąć zapisu duplikatu
        }
        User newUser = new User();
        newUser.setLogin(login);
        newUser.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));
        newUser.setEmail(email);
        newUser.setName(name);
        newUser.setSurname(surname);
        newUser.setRole(User.Role.USER);
        this.userRepository.save(newUser);
        this.httpSession.setAttribute("registerinfo", "Rejestracja zakończona pomyślnie");
        System.out.println("Zarejestrowano nowego użytkownika");
    }

    @Override
    public void logout() {
        this.httpSession.removeAttribute(SessionConstants.USER_KEY);
        System.out.println("Wylogowano");
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
