package model.Account;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by maxim on 02.10.18.
 */
@Entity
@Table(name = "account")
public class Account implements Serializable{

    Account(){

    }

    public Account(String login, String password, String salt, String access_token){
        this.login = login;
        this.password = password;
        this.salt = salt;
        this.access_token = access_token;

    }

    @Id
    @Column(name = "login")
    private String login;

    @Column(name = "password")
    private String password;

    @Column(name = "salt")
    private String salt;

    @Column(name = "access_token")
    private String access_token;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "friendship",
            joinColumns = @JoinColumn(name  = "firstAcc")

    )
    @Column(name = "secondAcc")
    private Set<String> friends = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "friend_request",
            joinColumns = @JoinColumn(name = "receiver")
    )
    @Column(name = "sender")
    private Set<String> inRequest = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "friend_request",
            joinColumns = @JoinColumn(name = "sender")
    )
    @Column(name = "receiver")
    private Set<String> outRequest = new HashSet<>();

    public Set<String> getFriends() {
        return friends;
    }

    public void setFriends(Set<String> friends) {
        this.friends = friends;
    }


    public Set<String> getInRequest() {
        return inRequest;
    }

    public void setInRequest(Set<String> inRequest) {
        this.inRequest = inRequest;
    }

    public Set<String> getOutRequest() {
        return outRequest;
    }

    public void setOutRequest(Set<String> outRequest) {
        this.outRequest = outRequest;
    }

    //private List<GameStatistic> games;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

}
