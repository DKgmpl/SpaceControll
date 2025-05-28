package pl.edu.wszib.dnogiec.spacecontroll.init;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import pl.edu.wszib.dnogiec.spacecontroll.dao.impl.spring.data.UserRepository;
import pl.edu.wszib.dnogiec.spacecontroll.model.User;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        String adminLogin = "admin";

        if (userRepository.findByLogin(adminLogin).isEmpty()) {
            User admin = new User();
            admin.setName("Admin");
            admin.setSurname("Adminowski");
            admin.setLogin(adminLogin);
            admin.setPassword(DigestUtils.md5DigestAsHex("admin123".getBytes()));
            admin.setEmail("admin@example.com");
            admin.setRole(User.Role.ADMIN);
            userRepository.save(admin);
            System.out.println("Utworzono użytkownika ADMIN: login=admin, hasło=admin123");
        }
    }
}
