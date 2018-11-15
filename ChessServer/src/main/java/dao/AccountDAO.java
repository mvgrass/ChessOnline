package dao;

import model.Account.Account;

/**
 * Created by maxim on 03.10.18.
 */
public interface AccountDAO {

    public boolean checkAuthorize(String token);

    public Account getAccountByToken(String token) throws UnauthorizedException;

    public boolean insert(Account account);

    public boolean update(Account account) throws UnauthorizedException;

    public boolean update(Account[] account) throws UnauthorizedException;

    public boolean deleteAccount(String token) throws UnauthorizedException;

    public Account getAccountByLogin(String login) throws WrongParameterException;

    public void addFriend(String token, String login) throws UnauthorizedException, WrongParameterException;

    public void deleteFriend(String token, String login) throws UnauthorizedException, WrongParameterException;

    public void deleteFriendRequest(String token, String login) throws UnauthorizedException, WrongParameterException;
}
