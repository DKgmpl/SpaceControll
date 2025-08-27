package pl.edu.wszib.dnogiec.spacecontroll.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.edu.wszib.dnogiec.spacecontroll.dao.impl.spring.data.UserRepository;
import pl.edu.wszib.dnogiec.spacecontroll.model.User;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Próba uwierzytelnienia dla użytkownika: " + username);
        try {
            String uname = username == null ? "" : username.trim().toLowerCase(java.util.Locale.ROOT);
            User user = userRepository.findByLogin(uname)
                    .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono Użytkownika z loginem: " + uname));

            System.out.println("Użytkownik znaleziony: " + user.getLogin() + ", Rola: " + user.getRole().name());
            return new org.springframework.security.core.userdetails.User(
                    user.getLogin(),
                    user.getPassword(),
                    Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()))
            );
        } catch (UsernameNotFoundException e) {
            System.out.println("Uwierzytelnianie nie powiodło się: " + e.getMessage());
            throw e;
        }
    }
}
