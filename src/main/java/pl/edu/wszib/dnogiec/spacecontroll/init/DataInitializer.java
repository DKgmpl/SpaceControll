package pl.edu.wszib.dnogiec.spacecontroll.init;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pl.edu.wszib.dnogiec.spacecontroll.dao.impl.spring.data.UserRepository;
import pl.edu.wszib.dnogiec.spacecontroll.model.User;

import java.sql.SQLOutput;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String adminLogin = "admin";
        String userLogin = "user";

        if (userRepository.findByLogin(adminLogin).isEmpty()) {
            User admin = new User();
            admin.setName("Admin");
            admin.setSurname("Adminowski");
            admin.setLogin(adminLogin);
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@example.com");
            admin.setRole(User.Role.ADMIN);
            userRepository.save(admin);
            System.out.println("Utworzono użytkownika ADMIN: login=admin, hasło=admin123");
        }

        if (userRepository.findByLogin(userLogin).isEmpty()) {
            User user = new User();
            user.setName("User");
            user.setSurname("Userowski");
            user.setLogin(userLogin);
            user.setPassword(passwordEncoder.encode("user123"));
            user.setEmail("user@example.com");
            user.setRole(User.Role.USER);
            userRepository.save(user);
            System.out.println("Utworzono użytkownika USER: login=user, hasło=user123");
        }
    }
}
