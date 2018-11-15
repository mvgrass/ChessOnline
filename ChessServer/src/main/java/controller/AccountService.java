package controller;

import Utils.Util;
import dao.UnauthorizedException;
import dao.WrongParameterException;
import model.Account.Account;
import dao.AccountDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * Created by maxim on 02.10.18.
 */
@RestController
@RequestMapping("/")
public class AccountService {

    @Autowired
    private AccountDAO accountDAO;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String welcome(HttpServletRequest request, HttpServletResponse response){
        return "Plug";
    }

    @RequestMapping(value = "api/account/signIn", method = RequestMethod.POST)
    public void signIn(HttpServletRequest request, HttpServletResponse response){
        String login = request.getParameter("login");
        String password = request.getParameter("password");

        try {

            Account account = accountDAO.getAccountByLogin(login);

            String hashPassword = Utils.Util.hash(password, account.getSalt());
            if (hashPassword.equals(account.getPassword())) {
                response.setStatus(200);

                Cookie cookie = new Cookie("Access_token", account.getAccess_token());
                cookie.setPath("/");
                response.addCookie(cookie);
            }else
                response.setStatus(400);
        }catch (WrongParameterException exc) {
            response.setStatus(400);
        }
    }

    @RequestMapping(value = "api/account/signUp", method = RequestMethod.POST)
    public void signUp(HttpServletRequest request, HttpServletResponse response){
        String login = request.getParameter("login");
        String password = request.getParameter("password");

        if(login!=null &&
                !("".equals(login))&&
                password!=null&&
                !("".equals(password))){


            String salt = Utils.Util.generateSalt();

            String hashPassword = Utils.Util.hash(password, salt);

            String token = Utils.Util.generateAccessToken(login, String.valueOf(new Date().getTime()));

            if(accountDAO.insert(new Account(login, hashPassword, salt, token)))
                this.signIn(request, response);
            else{
                response.setStatus(400);
            }
        }else{
            response.setStatus(400);
        }

    }


    //TODO
    @RequestMapping(value = "api/account/deleteAccount", method = RequestMethod.DELETE)
    public void deleteAccount(HttpServletRequest request, HttpServletResponse response){

    }

    @RequestMapping(value = "api/account/addFriend/{id}", method = RequestMethod.POST)
    public void addFriend(@PathVariable("id") String id, HttpServletRequest request,
                          HttpServletResponse response){

        Cookie[] cookies = request.getCookies();

        String access_token = null;
        for(int i = 0;i<cookies.length;++i){
            if("Access_token".equals(cookies[i].getName())){
                access_token = cookies[i].getValue();
            }
        }

        try {
            accountDAO.addFriend(access_token, id);
            response.setStatus(200);

        } catch (UnauthorizedException exc){
            response.setStatus(401);
        }catch (WrongParameterException exc){
            response.setStatus(400);
        }


    }


    @RequestMapping(value = "api/account/deleteFriend/{id}", method = RequestMethod.DELETE)
    public void deleteFriend(@PathVariable("id") String id, HttpServletRequest request,
                          HttpServletResponse response){

        Cookie[] cookies = request.getCookies();

        String access_token = null;
        for(int i = 0;i<cookies.length;++i){
            if("Access_token".equals(cookies[i].getName())){
                access_token = cookies[i].getValue();
                break;
            }
        }

        try{
            accountDAO.deleteFriend(access_token, id);
            response.setStatus(200);
        }catch (UnauthorizedException exc){
            response.setStatus(401);
        }catch (WrongParameterException exc){
            response.setStatus(400);
        }

    }


    @RequestMapping(value = "api/account/deleteFriendRequest/{id}", method = RequestMethod.DELETE)
    public void deleteFriendRequest(@PathVariable("id") String id, HttpServletRequest request,
                             HttpServletResponse response){

        Cookie[] cookies = request.getCookies();

        String access_token = null;
        for(int i = 0;i<cookies.length;++i){
            if("Access_token".equals(cookies[i].getName())){
                access_token = cookies[i].getValue();
                break;
            }
        }

        try{
            accountDAO.deleteFriendRequest(access_token, id);
            response.setStatus(200);
        }catch (UnauthorizedException exc){
            response.setStatus(401);
        }catch (WrongParameterException exc){
            response.setStatus(400);
        }

    }

    @RequestMapping(value = "api/account/getAccountInfo/{id}", method = RequestMethod.GET)
    public String getAccountInfo(@PathVariable("id") String id, HttpServletRequest request,
                               HttpServletResponse response){

        Cookie[] cookies = request.getCookies();

        String accessToken = "";

        for (Cookie cookie:cookies){
            if("Access_token".equals(cookie.getName())){
                accessToken = cookie.getValue();
                break;
            }
        }


        return null;
    }

    @RequestMapping(value = "api/account/getMyAccountInfo", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_ATOM_XML_VALUE)
    public String getMyAccountInfo(HttpServletRequest request, HttpServletResponse response){

        Cookie[] cookies = request.getCookies();

        String accessToken = "";

        for (Cookie cookie:cookies){
            if("Access_token".equals(cookie.getName())){
                accessToken = cookie.getValue();
                break;
            }
        }
        try {
            Account account = accountDAO.getAccountByToken(accessToken);

            response.setStatus(200);
            return Util.getInfoAboutMyAccountXML(account);

        }catch (UnauthorizedException exc){
            response.setStatus(401);
            return null;
        }
    }

}
