package pl.edu.wszib.dnogiec.spacecontroll.services;

public interface IAuthenticationService {

    void register(String email, String login, String password, String name, String surname);

    void showAllData();

    String getLoginInfo();

    String getRegisterInfo();
}
