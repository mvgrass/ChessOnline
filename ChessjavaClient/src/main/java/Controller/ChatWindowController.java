package Controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Pair;
import requests.AccountWebSocketEndpoint;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * Created by maxim on 30.10.18.
 */
public class ChatWindowController {
    private Map<String, Pair<Tab, ChatContentController>> openedDialogs = new HashMap<>();

    @FXML
    private MainWindowController parent;

    @FXML
    private TabPane tabPane;

    private AccountWebSocketEndpoint socketEndpoint;

    public void setSocketEndpoint(AccountWebSocketEndpoint socketEndpoint){
        this.socketEndpoint = socketEndpoint;
        openedDialogs.forEach((String login, Pair<Tab, ChatContentController> content)->{
            content.getValue().setSocketEndpoint(socketEndpoint);
        });
    }

    public void setParent(MainWindowController parent){
        this.parent = parent;
    }

    public void addChat(String name, Queue<String> messages){
        Pair<Tab, ChatContentController> pair = openedDialogs.get(name);
        ChatContentController contentController = null;
        if(pair!=null)
            contentController = pair.getValue();
        try {
            if (contentController == null) {
                String fxmlFile = "/fxml/ChatContent.fxml";
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                Pane root = (Pane) loader.load();

                contentController = (ChatContentController) loader.getController();
                contentController.setSocketEndpoint(this.socketEndpoint);
                contentController.setParent(this);

                Tab newTab = new Tab();
                newTab.setContent(root);
                newTab.setText(name);


                openedDialogs.put(name, new Pair<>(newTab, contentController));
                tabPane.getTabs().add(newTab);
            }

            contentController.addMessages(name, messages);
        }catch (IOException exc){
            exc.printStackTrace();
        }


    }

    public void addMessage(String name, String message){
        Pair<Tab, ChatContentController> pair = openedDialogs.get(name);
        if(pair!=null)
            pair.getValue().addMessage(name, message);
    }

    public void closeTab(String name){
        Pair<Tab, ChatContentController> pair = openedDialogs.get(name);
        tabPane.getTabs().remove(pair.getKey());
        openedDialogs.remove(name);

        parent.chatDialogClosed(name);

        if(openedDialogs.isEmpty()){
            Stage chatStage = ((Stage)tabPane.getScene().getWindow());
            chatStage.fireEvent(new WindowEvent(chatStage, WindowEvent.WINDOW_CLOSE_REQUEST));
        }
    }

    public void close(){
        Stage chatStage = ((Stage)tabPane.getScene().getWindow());
        chatStage.fireEvent(new WindowEvent(chatStage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }
}
