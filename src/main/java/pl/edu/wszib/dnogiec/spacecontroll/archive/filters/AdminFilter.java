//package pl.edu.wszib.dnogiec.spacecontroll.archive.filters;
//
//import jakarta.servlet.*;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import pl.edu.wszib.dnogiec.spacecontroll.model.User;
//import pl.edu.wszib.dnogiec.spacecontroll.session.SessionConstants;
//
//import java.io.IOException;
//import java.util.Set;
//
//public class AdminFilter implements Filter {
//
//    //Zestaw chronionych ścieżek
//    private static final Set<String> ADMIN_PROTECTED_PATHS = Set.of("/rooms/add");
//
//    @Override
//    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
//            throws IOException, ServletException {
//        HttpServletRequest req = (HttpServletRequest) request;
//        HttpServletResponse resp = (HttpServletResponse) response;
//        String path = req.getRequestURI();
//
//        boolean requiresLogin = ADMIN_PROTECTED_PATHS.stream().anyMatch(path::startsWith);
//
//        if (requiresLogin) {
//            User user = (User) req.getSession().getAttribute(SessionConstants.USER_KEY);
//            if (user == null || user.getRole() != User.Role.ADMIN) {
//                resp.sendRedirect("/login");
//                return;
//            }
//        }
//
//        chain.doFilter(request, response);
//    }
//}
