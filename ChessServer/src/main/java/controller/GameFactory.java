package controller;

import model.Account.GameManager;
import org.springframework.stereotype.Component;

/**
 * Created by maxim on 08.11.18.
 */

@Component
public interface GameFactory {
    public GameManager createGame(String gameId, String player1, String player2, Long duration);
}
