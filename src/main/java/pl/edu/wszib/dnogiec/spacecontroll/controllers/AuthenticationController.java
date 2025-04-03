package pl.edu.wszib.dnogiec.spacecontroll.controllers;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public String loginPost(@RequestParam String login, @RequestParam String password) {
        this.authenticationService.login(login, password);
        if (this.httpSession.getAttribute(SessionConstants.USER_KEY) != null) {
            return "redirect:/";
        }
        return "redirect:/login";
    }

    @GetMapping(path = "/register")
    public String registerGet(Model model) {
        model.addAttribute("registerInfo", this.authenticationService.getRegisterInfo());
        return "register";
    }

    @PostMapping(path = "/register")
    public String registerPost(@RequestParam String login, @RequestParam String password, Model model) {
        try {
            this.authenticationService.register(login, password);
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
