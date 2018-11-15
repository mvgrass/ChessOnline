package controller;

import Utils.ScheduledMap;
import Utils.ScheduledRemovingObserver;
import Utils.Util;
import dao.AccountDAO;
import dao.UnauthorizedException;
import dao.WrongParameterException;
import model.Account.Account;
import model.Account.GameManager;
import model.Game.ForbiddenMoveException;
import model.Game.Piece;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by maxim on 03.10.18.
 */

@Aspect
public class OnlineService extends TextWebSocketHandler
implements ScheduledRemovingObserver<String, Map.Entry<String,Long>>{

    @Autowired
    private AccountDAO accountDAO;

    @Autowired
    private GameFactory gameFactory;

    private Map<String, ScheduledMap<String, Map.Entry<String,Long>>> invites = new HashMap<>();

    private final Map<String, WebSocketSession> onlineUsers = new HashMap<>();

    private Map<String, GameManager> openedGames = new HashMap<>();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
        String token = (String)session.getHandshakeHeaders().get("Access_token").get(0);
        Account acc = accountDAO.getAccountByToken(token);

        String type = Util.getHeaderOfMessage(message.getPayload());

        switch (type){
            case "Message":
            {
                String To = Util.getMessageTo(message.getPayload());
                if(acc.getFriends().contains(To)) {
                    String messageToSend = Util.getMessageNotificationXML(acc.getLogin(),
                            Util.getTextOfMessage(message.getPayload()));

                    WebSocketSession sessionTo;
                    synchronized (onlineUsers) {
                        sessionTo = onlineUsers.get(To);
                    }

                    if (sessionTo != null)
                        sessionTo.sendMessage(new TextMessage(messageToSend));
                }
                break;
            }
            case "Invite":
            {
                String To = Util.getMessageTo(message.getPayload());
                Long Duration = Long.valueOf(Util.getInviteDuration(message.getPayload()));
                if(acc.getFriends().contains(To)){
                    String notification = Util.getInviteNotificationXML(acc.getLogin());

                    WebSocketSession sessionTo;
                    synchronized (onlineUsers) {
                        sessionTo = onlineUsers.get(To);
                    }

                    ScheduledMap<String, Map.Entry<String,Long>> scheduledMap = invites.get(acc.getLogin());
                    if(scheduledMap == null){
                        scheduledMap = invites.put(acc.getLogin(),
                                new ScheduledMap<String, Map.Entry<String, Long>>(new HashMap<String, Map.Entry<String,Long>>(), (long)30000));
                    }

                    synchronized (scheduledMap){
                        scheduledMap.put(To, new AbstractMap.SimpleEntry<String, Long>(acc.getLogin(),Duration));
                    }

                    if (sessionTo != null)
                        sessionTo.sendMessage(new TextMessage(notification));
                }
                break;
            }

            case "AcceptInvite":
            {
                String from = Util.getMessageFrom(message.getPayload());
                try {
                    Long duration = invites.get(from).get(acc.getLogin()).getValue();

                    WebSocketSession session2;
                    synchronized (onlineUsers) {
                        session2 = onlineUsers.get(from);
                    }

                    if (session2 != null) {

                        String gameId = acc.getLogin()+from+System.currentTimeMillis();

                        GameManager game = gameFactory.createGame(gameId, acc.getLogin(), from, duration);

                        String lights = game.getLightsPlayer();

                        String notification1;
                        String notification2;
                        if(lights.equals(from)){
                            notification1 = Util.getGameStartedNotificationXML(from, gameId, "Dark", duration);
                            notification2 = Util.getGameStartedNotificationXML(acc.getLogin(), gameId, "Light", duration);
                        }else{
                            notification1 = Util.getGameStartedNotificationXML(from, gameId, "Light", duration);
                            notification2 = Util.getGameStartedNotificationXML(acc.getLogin(), gameId, "Dark", duration);
                        }

                        session.sendMessage(new TextMessage(notification1));
                        session2.sendMessage(new TextMessage(notification2));

                        openedGames.put(gameId, game);
                        game.start();
                    }
                }catch (RuntimeException exc){
                    exc.printStackTrace();
                }
                break;
            }
            case "Move":
            {
                String start = Util.getMessageFrom(message.getPayload());
                String end = Util.getMessageTo(message.getPayload());
                String gameId = Util.getGameId(message.getPayload());
                String transform = Util.getTransform(message.getPayload());

                GameManager game = openedGames.get(gameId);
                if(game!=null && !game.isEnd()){
                    try {
                        game.move(acc.getLogin(), start, end, transform);

                        String firstPlayer = game.getLightsPlayer();
                        String secondPlayer = game.getDarksPlayer();

                        WebSocketSession session1;
                        WebSocketSession session2;

                        synchronized (onlineUsers){
                            session1 = onlineUsers.get(firstPlayer);
                            session2 = onlineUsers.get(secondPlayer);
                        }

                        String notification = Util.getGameStateXML(gameId, game);

                        if(session1!=null)
                            session1.sendMessage(new TextMessage(notification));

                        if(session2!=null)
                            session2.sendMessage(new TextMessage(notification));

                        if(game.isEnd()){
                            String endOfGameNotification = Util.getEndOfGameXML(gameId, game.getWinner(), "Mate");
                            openedGames.remove(gameId);

                            if(session1!=null)
                                session1.sendMessage(new TextMessage(endOfGameNotification));

                            if(session2!=null)
                                session2.sendMessage(new TextMessage(endOfGameNotification));
                        }

                    }catch (ForbiddenMoveException exc){

                    }
                }
                break;
            }
            case "GiveUp":
            {
                String gameId = Util.getGameId(message.getPayload());

                GameManager game = openedGames.get(gameId);
                if(game!=null && !game.isEnd()){

                    game.giveUp(acc.getLogin());

                    String firstPlayer = game.getLightsPlayer();
                    String secondPlayer = game.getDarksPlayer();

                    WebSocketSession session1;
                    WebSocketSession session2;

                    synchronized (onlineUsers){
                        session1 = onlineUsers.get(firstPlayer);
                        session2 = onlineUsers.get(secondPlayer);
                    }

                    if(game.isEnd()){
                        String endOfGameNotification = Util.getEndOfGameXML(gameId, game.getWinner(), "GivingUp");
                        openedGames.remove(gameId);

                        if(session1!=null)
                            session1.sendMessage(new TextMessage(endOfGameNotification));

                        if(session2!=null)
                            session2.sendMessage(new TextMessage(endOfGameNotification));
                    }
                }
                break;
            }

            case "GetUpdate":
            {

                String gameId = Util.getGameId(message.getPayload());

                GameManager game = openedGames.get(gameId);
                if(game!=null && !game.isEnd()){

                    String firstPlayer = game.getLightsPlayer();
                    String secondPlayer = game.getDarksPlayer();

                    if(firstPlayer.equals(acc.getLogin())||secondPlayer.equals(acc.getLogin())) {

                        String notification = Util.getGameStateXML(gameId, game);

                        session.sendMessage(new TextMessage(notification));
                    }
                }
                break;
            }

        }

    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        super.handlePongMessage(session, message);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        String token = (String)session.getHandshakeHeaders().get("Access_token").get(0);
        Account acc = accountDAO.getAccountByToken(token);

        if (acc!=null) {
            synchronized (onlineUsers) {
                onlineUsers.remove(acc.getLogin());
            }

            invites.remove(acc.getLogin());

            noticeAboutOffline(acc);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);

        String token = (String)session.getHandshakeHeaders().get("Access_token").get(0);

        if(token!=null) {
            Account acc = accountDAO.getAccountByToken(token);

            if (acc!=null) {
                synchronized (onlineUsers) {
                    onlineUsers.put(acc.getLogin(), session);
                }

                if(!invites.containsKey(acc.getLogin())) {
                    ScheduledMap<String, Map.Entry<String,Long>> scheduledMap =
                            new ScheduledMap<String, Map.Entry<String,Long>>(new HashMap<String, Map.Entry<String,Long>>(), (long)30000);
                    scheduledMap.addListener(this);
                    invites.put(acc.getLogin(), scheduledMap);
                }

                noticeAboutOnline(acc, session);
            } else {
                session.close();
            }
        }
    }

    private void noticeAboutOnline(Account acc, WebSocketSession session) throws IOException{
        String message = Util.getOnlineNotificationXML(acc.getLogin());
        synchronized (onlineUsers){
            for (String friend: acc.getFriends()){
                if(onlineUsers.containsKey(friend)){
                    onlineUsers.get(friend).sendMessage(new TextMessage(message));
                    session.sendMessage(new TextMessage(Util.getOnlineNotificationXML(friend)));
                }
            }
        }
    }

    private void noticeAboutOffline(Account acc) throws IOException{
        String message = Util.getOfflineNotificationXML(acc.getLogin());
        synchronized (onlineUsers){
            for (String friend: acc.getFriends()){
                if(onlineUsers.containsKey(friend)){
                    onlineUsers.get(friend).sendMessage(new TextMessage(message));
                }
            }
        }
    }

    public void noticeAboutTimeOut(String gameId, String acc1, String acc2, Piece.COLOR color) {
        String winner = (color == Piece.COLOR.LIGHT) ? "Light" : "Dark";
        String notification = Util.getEndOfGameXML(gameId, winner, "TimeOut");

        WebSocketSession session1;
        WebSocketSession session2;

        synchronized (onlineUsers) {
            session1 = onlineUsers.get(acc1);
            session2 = onlineUsers.get(acc2);
        }

        synchronized (openedGames) {
            openedGames.remove(gameId);
        }

        try {


            if (session1 != null)
                session1.sendMessage(new TextMessage(notification));

            if (session2 != null)
                session2.sendMessage(new TextMessage(notification));

        }catch (IOException exc){
            exc.printStackTrace();
        }
    }

    @After("execution(** controller.AccountService.addFriend(String,..)) " +
            "&& args(id, ..)")
    private void noticeAboutAdding(String id) throws IOException{
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes)requestAttributes).getRequest();
        HttpServletResponse response = ((ServletRequestAttributes)requestAttributes).getResponse();


        if(response.getStatus() == 200){
            String token = null;
            for(Cookie cookie : request.getCookies()){
                if("Access_token".equals(cookie.getName())){
                    token = cookie.getValue();
                }
            }
            try {
                Account acc1 = accountDAO.getAccountByToken(token);
                Account acc2 = accountDAO.getAccountByLogin(id);

                String addingMessage = Util.getAddingNotificationXML(acc1.getLogin());
                String acc1OnlineMessage = Util.getOnlineNotificationXML(acc1.getLogin());
                String acc2OnlineMessage = Util.getOnlineNotificationXML(acc2.getLogin());
                synchronized(onlineUsers){
                    WebSocketSession acc2Session = onlineUsers.get(acc2.getLogin());
                    if(acc2Session != null){
                        acc2Session.sendMessage(new TextMessage(addingMessage));

                        if(acc1.getFriends().contains(acc2.getLogin())) {
                            WebSocketSession acc1Session = onlineUsers.get(acc1.getLogin());
                            if (acc1Session != null) {
                                acc1Session.sendMessage(new TextMessage(acc2OnlineMessage));
                                acc2Session.sendMessage(new TextMessage(acc1OnlineMessage));
                            }
                        }
                    }
                }

            }catch (UnauthorizedException | WrongParameterException exc){
                exc.printStackTrace();
            }
        }
    }

    @After("(execution(** controller.AccountService.deleteFriend(String,..)) " +
            "&& args(id, ..)) ||" +
            "(execution(** controller.AccountService.deleteFriendRequest(String,..)) " +
            "&& args(id, ..))")
    private void noticeAboutRemoving(String id) throws IOException{
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes)requestAttributes).getRequest();
        HttpServletResponse response = ((ServletRequestAttributes)requestAttributes).getResponse();

        if(response.getStatus() == 200) {
            try {
                String token = null;
                for(Cookie cookie : request.getCookies()){
                    if("Access_token".equals(cookie.getName())){
                        token = cookie.getValue();
                    }
                }
                Account acc1 = accountDAO.getAccountByToken(token);
                Account acc2 = accountDAO.getAccountByLogin(id);

                String message = Util.getRemovingNotificationXML(acc1.getLogin());
                synchronized (onlineUsers) {
                    WebSocketSession acc2Session = onlineUsers.get(acc2.getLogin());
                    if(acc2Session != null){
                        acc2Session.sendMessage(new TextMessage(message));
                    }
                }
            }catch (WrongParameterException | UnauthorizedException exc){
                exc.printStackTrace();
            }
        }
    }

    @Override
    public void update(Map.Entry<String, Map.Entry<String, Long>> removed) {
        String notification = Util.getInviteTimedOutNotificationXML(removed.getValue().getKey());
        WebSocketSession session;
        synchronized (onlineUsers){
            session = onlineUsers.get(removed.getKey());
        }

        try {
            if(session != null) {
                session.sendMessage(new TextMessage(notification));
            }
        }catch (IOException exc){
            exc.printStackTrace();
        }
    }
}
