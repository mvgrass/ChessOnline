package Controller;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by maxim on 15.10.18.
 */
public class SceneController {
    private Map<String, String> paneMap = new HashMap<>();
    private Scene mainScene = null;

    private static SceneController INSTANCE = null;

    public static SceneController getInstance(){
        if(INSTANCE == null){
            INSTANCE = new SceneController();
        }

        return INSTANCE;
    }

    private SceneController(){
    }

    public void setScene(Scene scene){
        mainScene = scene;
    }

    public void add(String name, String panePath){
        paneMap.put(name, panePath);
    }

    public void remove(String name){
        paneMap.remove(name);
    }

    public void activate(String name){

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource(paneMap.get(name)));

            final Pane pane = (Pane) loader.load();

            if (pane != null) {
                FadeTransition ft1 = new FadeTransition(Duration.millis(500), mainScene.getRoot());
                ft1.setFromValue(1.0);
                ft1.setToValue(0.0);

                FadeTransition ft2 = new FadeTransition(Duration.millis(1500), pane);
                ft2.setFromValue(0.0);
                ft2.setToValue(1.0);


                Unresizable controller = (Unresizable) loader.getController();

                ft1.setOnFinished(new javafx.event.EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        pane.setOpacity(0.0);
                        mainScene.setRoot(pane);
                        controller.setSize();
                        ft2.play();
                    }
                });

                ft1.play();
            }
        }catch (IOException | RuntimeException exc){
            exc.printStackTrace();
        }
    }
}
