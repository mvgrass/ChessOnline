import Controller.SceneController;
import Controller.SignInController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.Optional;

/**
 * Created by maxim on 03.10.18.
 */
public class Main extends Application{
    public static void main(String[] args) throws Exception {
            launch(args);
        }

    @Override
    public void start(Stage stage) throws Exception {
        String fxmlFile = "/fxml/SignInWindow.fxml";
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Pane root = (Pane)loader.load();

        Scene mainScene = new Scene(root);

        SceneController.getInstance().setScene(mainScene);
        loadScenes();

        SignInController controller = (SignInController)loader.getController();

        stage.setOnCloseRequest((WindowEvent event)->{
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText("Exit");
            alert.setContentText("Are you sure you want to exit?");
            Optional<ButtonType> choice = alert.showAndWait();

            if (choice.get() == ButtonType.OK)
                Platform.exit();
            else
                event.consume();
        });
        stage.setTitle("Chess");
        stage.setScene(mainScene);
        stage.getIcons().add(new Image(Main.class.getResourceAsStream("/images/appIcon.png")));
        stage.show();

        controller.setSize();
    }

    private void loadScenes() throws Exception{
        SceneController sceneController = SceneController.getInstance();

        sceneController.add("signInWindow", "/fxml/SignInWindow.fxml");
        sceneController.add("mainWindow", "/fxml/MainWindow.fxml");

    }
}

