package Model;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by maxim on 20.10.18.
 */
public class Account {
    private String login;

    private List<String> friends = new LinkedList<>();

    private List<String> inRequests = new LinkedList<>();

    private List<String> outRequests = new LinkedList<>();

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public List<String> getFriends() {
        return friends;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends;
    }

    public List<String> getInRequests() {
        return inRequests;
    }

    public void setInRequests(List<String> inRequests) {
        this.inRequests = inRequests;
    }

    public List<String> getOutRequests() {
        return outRequests;
    }

    public void setOutRequests(List<String> outRequests) {
        this.outRequests = outRequests;
    }
}
