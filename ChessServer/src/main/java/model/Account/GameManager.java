package model.Account;

import controller.OnlineService;
import model.Game.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

/**
 * Created by maxim on 05.11.18.
 */
@Component
public class GameManager {

    @Autowired
    private OnlineService onlineService;

    volatile private Long lightsTimeLeft;

    volatile private Long darksTimeLeft;

    volatile private boolean lightsWon = false;

    volatile private boolean darksWon = false;

    volatile private Piece.COLOR turn;

    private Object mutex;

    private Game game;

    private LightsTicker lightsTicker;

    private DarksTicker darksTicker;

    public Long getLightsTimeLeft() {
        return lightsTimeLeft;
    }

    public Long getDarksTimeLeft() {
        return darksTimeLeft;
    }

    public String getLightsPlayer() {
        return lightsPlayer;
    }

    public String getDarksPlayer() {
        return darksPlayer;
    }

    private String lightsPlayer;

    private String darksPlayer;

    private String gameId;

    public GameManager(String gameId, String fPlayer, String sPlayer, Long millies){

        this.gameId = gameId;

        if(new Random().nextBoolean()){
            lightsPlayer = fPlayer;
            darksPlayer = sPlayer;
        }else{
            lightsPlayer = sPlayer;
            darksPlayer = fPlayer;
        }

        lightsTimeLeft = millies;
        darksTimeLeft = millies;

        game = new Game();
        turn = Piece.COLOR.LIGHT;
        mutex = new Object();
    }

    public void start(){
        lightsTicker = new LightsTicker();
        darksTicker = new DarksTicker();

        lightsTicker.setDaemon(true);
        darksTicker.setDaemon(true);

        lightsTicker.start();
        darksTicker.start();
    }

    public List<List<Piece>> getBoard(){
        return game.getDesk();
    }

    public Piece.COLOR getTurn(){
        return game.turn();
    }

    public boolean isEnd(){
        return game.isMate()
                || game.isDraw()
                || lightsWon
                ||darksWon;
    }

    public String getWinner(){
        if(lightsWon)
            return "Light";
        else if(darksWon)
            return "Dark";
        else
            return "";
    }

    public String getLastMoveString(){
        return game.getLastMoveString();
    }

    public void move(String who, String from, String to, String transform) throws ForbiddenMoveException{
        if(isEnd())
            throw new ForbiddenMoveException();

        int startX = from.charAt(0) - 'A';
        int startY = from.charAt(1) - '1';
        int endX = to.charAt(0) - 'A';
        int endY = to.charAt(1) - '1';

        Piece new_piece = null;
        if(lightsPlayer.equals(who)){
            if(turn== Piece.COLOR.LIGHT){

                if (transform!=null) {
                    switch (transform) {
                        case "Q":
                            new_piece = new Queen(endX, endY, Piece.COLOR.LIGHT);
                            break;
                        case "R":
                            new_piece = new Rogue(endX, endY, Piece.COLOR.LIGHT);
                            break;
                        case "B":
                            new_piece = new Bishop(endX, endY, Piece.COLOR.LIGHT);
                            break;
                        case "Kn":
                            new_piece = new Knight(endX, endY, Piece.COLOR.LIGHT);
                            break;
                    }
                }
            }else
                throw new ForbiddenMoveException();
        }else if(darksPlayer.equals(who)){
            if(turn== Piece.COLOR.DARK){
                if(transform!=null) {
                    switch (transform) {
                        case "Q":
                            new_piece = new Queen(endX, endY, Piece.COLOR.DARK);
                            break;
                        case "R":
                            new_piece = new Rogue(endX, endY, Piece.COLOR.DARK);
                            break;
                        case "B":
                            new_piece = new Bishop(endX, endY, Piece.COLOR.DARK);
                            break;
                        case "Kn":
                            new_piece = new Knight(endX, endY, Piece.COLOR.DARK);
                            break;
                    }
                }
            }else
                throw new ForbiddenMoveException();
        }else
            throw new ForbiddenMoveException();

        game.move(startX, startY, endX, endY, new_piece);
        if(game.isMate()){
            if(game.turn() == Piece.COLOR.LIGHT)
                darksWon = true;
            else
                lightsWon = true;

            lightsTicker.interrupt();
            darksTicker.interrupt();
        }

        turn = game.turn();
    }

    public void giveUp(String whom){
        if(lightsPlayer.equals(whom))
            darksWon = true;
        else if(darksPlayer.equals(whom))
            lightsWon = true;

    }

    class LightsTicker extends Thread {

        public void run() {
            synchronized (mutex) {
                while (!isEnd() && !isInterrupted()) {
                    try {
                        if (turn == Piece.COLOR.LIGHT) {
                            Thread.sleep(200);
                            lightsTimeLeft = Math.max(lightsTimeLeft - 200, 0);
                            if (lightsTimeLeft <= 0) {
                                darksWon = true;
                                onlineService.noticeAboutTimeOut(gameId, lightsPlayer, darksPlayer, Piece.COLOR.DARK);
                                mutex.notify();
                            }
                        } else {
                            mutex.notify();
                            mutex.wait();
                        }
                    } catch (InterruptedException exc) {
                        mutex.notify();
                    }
                }
            }
        }
    }

    class DarksTicker extends Thread{

        public void run(){
            synchronized (mutex) {
                while (!isEnd()&&!isInterrupted()) {
                    try {
                        if (turn == Piece.COLOR.DARK) {
                            Thread.sleep(200);
                            darksTimeLeft = Math.max(darksTimeLeft - 200, 0);
                            if(darksTimeLeft<=0) {
                                lightsWon = true;
                                onlineService.noticeAboutTimeOut(gameId, lightsPlayer, darksPlayer, Piece.COLOR.LIGHT);
                                mutex.notify();
                            }
                        }else{
                            mutex.notify();
                            mutex.wait();
                        }
                    }catch (InterruptedException exc){
                        mutex.notify();
                    }
                }
            }
        }
    }
}
