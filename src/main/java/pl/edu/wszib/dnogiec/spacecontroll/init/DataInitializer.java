package pl.edu.wszib.dnogiec.spacecontroll.init;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pl.edu.wszib.dnogiec.spacecontroll.dao.impl.spring.data.UserRepository;
import pl.edu.wszib.dnogiec.spacecontroll.model.User;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String adminLogin = "admin";
        String adminPassword = "admin123";
        String adminEmail = "admin@example.com";
        String userLogin = "user";
        String userPassword = "user123";
        String userEmail = "user@example.com";

        if (userRepository.findByLogin(adminLogin).isEmpty()) {
            User admin = new User();
            admin.setName("Admin");
            admin.setSurname("Adminowski");
            admin.setLogin(adminLogin.toLowerCase(Locale.ROOT));
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setEmail(adminEmail.toLowerCase(Locale.ROOT));
            admin.setRole(User.Role.ADMIN);
            userRepository.save(admin);
            System.out.println("Utworzono użytkownika ADMIN: login=admin, hasło=admin123");
        }

        if (userRepository.findByLogin(userLogin).isEmpty()) {
            User user = new User();
            user.setName("User");
            user.setSurname("Userowski");
            user.setLogin(userLogin.toLowerCase(Locale.ROOT));
            user.setPassword(passwordEncoder.encode(userPassword));
            user.setEmail(userEmail.toLowerCase(Locale.ROOT));
            user.setRole(User.Role.USER);
            userRepository.save(user);
            System.out.println("Utworzono użytkownika USER: login=user, hasło=user123");
        }
    }
}
