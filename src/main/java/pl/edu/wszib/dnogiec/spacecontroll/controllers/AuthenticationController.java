package pl.edu.wszib.dnogiec.spacecontroll.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import pl.edu.wszib.dnogiec.spacecontroll.dto.RegistrationForm;
import pl.edu.wszib.dnogiec.spacecontroll.exceptions.RegisterValidationExemption;
import pl.edu.wszib.dnogiec.spacecontroll.services.IAuthenticationService;

@Controller
@RequiredArgsConstructor
public class AuthenticationController {

    private final IAuthenticationService authenticationService;

    @GetMapping(path = "/login")
    public String loginGet(Model model) {
        model.addAttribute("loginInfo", this.authenticationService.getLoginInfo());
        return "login";
    }

    // Login POST is now handled by Spring Security

    @GetMapping(path = "/register")
    public String registerGet(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        model.addAttribute("registerInfo", this.authenticationService.getRegisterInfo());
        return "register";
    }

    @PostMapping(path = "/register")
    public String registerPost(@Valid @ModelAttribute("registrationForm") RegistrationForm registrationForm, 
                              BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("registrationSuccessful", false);
            // Pozostawiamy widok rejestracji – błędy będą wyświetlone
            return "register";
        }
        try {
            this.authenticationService.register(registrationForm.getEmail(),
                    registrationForm.getLogin(),
                    registrationForm.getPassword(),
                    registrationForm.getName(),
                    registrationForm.getSurname());
            model.addAttribute("registerInfo", "Rejestracja zakończona pomyślnie");
            model.addAttribute("registrationSuccessful", true);
            return "redirect:/login?registered=true";
        } catch (RegisterValidationExemption e) {
            model.addAttribute("registerInfo", e.getMessage());
            model.addAttribute("registrationSuccessful", false);
            return "register";
        }
    }
    // Logout is now handled by Spring Security
}
