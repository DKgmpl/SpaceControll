package pl.edu.wszib.dnogiec.spacecontroll.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pl.edu.wszib.dnogiec.spacecontroll.model.User;
import pl.edu.wszib.dnogiec.spacecontroll.session.SessionConstants;

import java.io.IOException;

public class AdminFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // Przykład: Jeżeli ścieżka jest publiczna (np. /login lub /register), pomijamy filtr
        String path = req.getRequestURI();
        if (path.startsWith("/login") || path.startsWith("/register") || path.startsWith("/")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession httpSession = req.getSession();

        if (httpSession == null ||
                !(httpSession.getAttribute(SessionConstants.USER_KEY) instanceof User u) ||
                u.getRole() != User.Role.ADMIN) {
            // Użytkownik nie ma uprawnień - przekierowanie
            resp.sendRedirect("/login");
            return;
        }

        // Użytkownik ma uprawnienia – kontynuacja przetwarzania żądania
        chain.doFilter(request, response);
    }
}
