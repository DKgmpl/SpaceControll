package pl.edu.wszib.dnogiec.spacecontroll.services;

public interface IAuthenticationService {
    // Spring Security now handles login and logout

    void register(String email, String login, String password, String name, String surname);

    void showAllData();

    String getLoginInfo();

    String getRegisterInfo();
}
