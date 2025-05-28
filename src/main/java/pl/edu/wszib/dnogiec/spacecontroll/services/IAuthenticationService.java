package pl.edu.wszib.dnogiec.spacecontroll.services;

public interface IAuthenticationService {
    void login(String login, String password);

    void register(String email, String login, String password, String name, String surname);

    void logout();

    void showAllData();

    String getLoginInfo();

    String getRegisterInfo();
}
