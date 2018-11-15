import controller.GameFactory;
import controller.OnlineService;
import model.Account.GameManager;
import dao.AccountDAO;
import dao.AccountDAOImpl;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.*;

/**
 * Created by maxim on 03.10.18.
 */
@Configuration
public class RootConfig {
    @Autowired
    BeanFactory beanFactory;

    @Bean
    AccountDAO AccountDAO(){
        return new AccountDAOImpl(SessionFactory());
    }

    @Bean
    SessionFactory SessionFactory(){
        return new org.hibernate.cfg.Configuration().configure().buildSessionFactory();
    }


    @Bean
    GameFactory gameFactory(){
        return new GameFactory() {
            @Override
            public GameManager createGame(String gameId, String player1, String player2, Long duration) {
                return beanFactory.getBean(GameManager.class, gameId, player1, player2, duration);
            }
        };
    }


    @Bean
    @Scope(value = "prototype")
    GameManager gameManager(String gameId, String player1, String player2, Long duration){
        return new GameManager(gameId, player1,player2, duration);
    }

    @Bean
    @Qualifier(value = "OnlineService")
    OnlineService onlineService(){return new OnlineService();}
}
