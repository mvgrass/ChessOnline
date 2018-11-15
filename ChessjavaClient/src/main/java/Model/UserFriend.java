package Model;

import javafx.scene.image.Image;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by maxim on 28.10.18.
 */
public class UserFriend implements Comparable {

    public enum Status{ONLINE, OFFLINE, INREQUEST , UNCONFIRMED};

    private String name;

    private Status status;

    private Image image;

    private Queue<String> messages = new LinkedList<>();

    public UserFriend(String name, Status status){
        this.name = name;
        this.status = status;

        switch (status){
            case ONLINE:
                image = new Image(UserFriend.class.getResourceAsStream("/images/onlineStatus.png"));
                break;
            case OFFLINE:
                image = new Image(UserFriend.class.getResourceAsStream("/images/offlineStatus.png"));
                break;
            case INREQUEST:
                image = new Image(UserFriend.class.getResourceAsStream("/images/inRequest.png"));
                break;
            case UNCONFIRMED:
                image = new Image(UserFriend.class.getResourceAsStream("/images/unconfirmedStatus.png"));
                break;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;

        switch (status){
            case ONLINE:
                image = new Image(UserFriend.class.getResourceAsStream("/images/onlineStatus.png"));
                break;
            case OFFLINE:
                image = new Image(UserFriend.class.getResourceAsStream("/images/offlineStatus.png"));
                break;
            case INREQUEST:
                image = new Image(UserFriend.class.getResourceAsStream("/images/inRequest.png"));
                break;
            case UNCONFIRMED:
                image = new Image(UserFriend.class.getResourceAsStream("/images/unconfirmedStatus.png"));
                break;
        }
    }

    public Image getImage(){
        return this.image;
    }

    public void addMessage(String message){
        messages.offer(message);
    }

    public Queue<String> getMessages(){
        return this.messages;
    }

    @Override
    public int compareTo(Object o) {
        if(!(o instanceof UserFriend))
            throw new RuntimeException("Incomparable classes");

        if(o == this)
            return 0;

        UserFriend other = (UserFriend) o;

        if(this.status == Status.ONLINE){
            if(other.status == Status.ONLINE)
                return name.compareTo(other.name);
            else
                return -1;
        }else if(this.status == Status.OFFLINE){
            if(other.status == Status.ONLINE)
                return 1;
            else if(other.status == Status.OFFLINE)
                return name.compareTo(other.name);
            else
                return -1;
        }else if(this.status == Status.INREQUEST){
            if(other.status == Status.ONLINE || other.status == Status.OFFLINE)
                return 1;
            else if(other.status == Status.INREQUEST)
                return name.compareTo(other.name);
            else
                return -1;
        }else{
            if(other.status == Status.UNCONFIRMED)
                return name.compareTo(other.name);
            else
                return 1;
        }
    }
}
