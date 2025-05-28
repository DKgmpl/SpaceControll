package pl.edu.wszib.dnogiec.spacecontroll.filters;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import pl.edu.wszib.dnogiec.spacecontroll.model.User;
import pl.edu.wszib.dnogiec.spacecontroll.session.SessionConstants;

import java.io.IOException;
import java.util.Set;

@Component
public class UserFilter implements Filter {

    //Zestaw chronionych ścieżek
    private static final Set<String> USER_PROTECTED_PATHS = Set.of("/rooms/*", "/reservations", "/reservation_create");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String path = req.getRequestURI();

        boolean requiresLogin = USER_PROTECTED_PATHS.stream().anyMatch(path::startsWith);

        if (requiresLogin) {
            User user = (User) req.getSession().getAttribute(SessionConstants.USER_KEY);
            if (user == null) {
                resp.sendRedirect("/login");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
