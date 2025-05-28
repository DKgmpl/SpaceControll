package pl.edu.wszib.dnogiec.spacecontroll.controllers;

import jakarta.servlet.http.HttpSession;
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
import pl.edu.wszib.dnogiec.spacecontroll.session.SessionConstants;

@Controller
@RequiredArgsConstructor
public class AuthenticationController {

    private final IAuthenticationService authenticationService;
    private final HttpSession httpSession;

    @GetMapping(path = "/login")
    public String loginGet(Model model) {
        model.addAttribute("loginInfo", this.authenticationService.getLoginInfo());
        return "login";
    }

    @PostMapping(path = "/login")
    public String loginPost(@ModelAttribute("login") String login, @ModelAttribute("password") String password) {
        this.authenticationService.login(login, password);
        if (this.httpSession.getAttribute(SessionConstants.USER_KEY) != null) {
            return "redirect:/";
        }
        return "redirect:/login";
    }

    @GetMapping(path = "/register")
    public String registerGet(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        model.addAttribute("registerInfo", this.authenticationService.getRegisterInfo());
        return "register";
    }

    @PostMapping(path = "/register")
    public String registerPost(@Valid @ModelAttribute("registrationForm") RegistrationForm registrationForm, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("registrationSuccesful", false);
            // Pozostawiamy widok rejestracji – błędy będą wyświetlone
            return "register";
        }
        try {
            this.authenticationService.register(registrationForm.getEmail(),
                    registrationForm.getLogin(),
                    registrationForm.getPassword(),
                    registrationForm.getName(),
                    registrationForm.getSurname());
            this.authenticationService.showAllData();
            model.addAttribute("registerInfo", "Rejestracja zakończona pomyślnie");
            model.addAttribute("registrationSuccesful", true);
        } catch (RegisterValidationExemption e) {
            model.addAttribute("registerInfo", e.getMessage());
            model.addAttribute("registrationSuccesful", false);
            e.printStackTrace();
            return "register";
        }

        if (this.httpSession.getAttribute(SessionConstants.USER_KEY) != null) {
            return "redirect:/";
        }

        return "register";
    }

    @GetMapping(path = "/logout")
    public String logout() {
        this.authenticationService.logout();
        return "redirect:/";
    }
}
