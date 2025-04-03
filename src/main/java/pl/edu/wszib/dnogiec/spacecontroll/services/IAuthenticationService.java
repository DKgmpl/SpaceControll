package pl.edu.wszib.dnogiec.spacecontroll.services;

public interface IAuthenticationService {
    void login(String login, String password);
    void register(String login, String password);
    void logout();
    String getLoginInfo();
    String getRegisterInfo();
}
