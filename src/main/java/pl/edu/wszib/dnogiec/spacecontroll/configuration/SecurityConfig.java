package pl.edu.wszib.dnogiec.spacecontroll.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.util.HashMap;
import java.util.Map;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            System.out.println("Użytkownik: " + authentication.getName() + " zalogowany");
            response.sendRedirect("/");
        };
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            System.out.println("Logowanie nie powiodło się: " + exception.getMessage());
            response.sendRedirect("/login?error=true");
        };
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return (request, response, authentication) -> {
            if (authentication != null) {
                System.out.println("Użytkownik: " + authentication.getName() + " wylogowany");
            } else {
                System.out.println("Użytkownik: unknown logout");
            }
            response.sendRedirect("/");
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/login", "/register", "/css/**", "/js/**").permitAll()
                .requestMatchers("/rooms/add").hasAuthority("ADMIN")
                .requestMatchers("/rooms/**", "/reservations", "/reservation_create").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("login")
                .passwordParameter("password")
                .successHandler(authenticationSuccessHandler())
                .failureHandler(authenticationFailureHandler())
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessHandler(logoutSuccessHandler())
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Create a map of encoders with id as key
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        // Add MD5 encoder for backward compatibility with existing passwords
        encoders.put("md5", new MessageDigestPasswordEncoder("MD5"));
        // Add BCrypt encoder as the preferred encoder for new passwords
        encoders.put("bcrypt", new BCryptPasswordEncoder());

        // Use DelegatingPasswordEncoder with bcrypt as the default encoder
        return new DelegatingPasswordEncoder("bcrypt", encoders);
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(userDetailsService)
               .passwordEncoder(passwordEncoder());
        return builder.build();
    }
}
