package requests;

import Model.Account;

/**
 * Created by maxim on 16.10.18.
 */
public interface RequestAPI {
    public void signIn(String login, String password) throws RequestException, WrongParameterException;

    public void signUp(String login, String password) throws RequestException, WrongParameterException;

    public void addFriend(String name) throws RequestException, WrongParameterException, UnauthorizedException;

    public void deleteFriend(String name) throws RequestException, WrongParameterException, UnauthorizedException;

    public void deleteFriendRequest(String name) throws RequestException, WrongParameterException, UnauthorizedException;

    public Account getMyAccountInfo() throws RequestException, UnauthorizedException;

    public String getToken();

    public String getHost();
}
