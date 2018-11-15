package Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import requests.AccountWebSocketEndpoint;

import java.util.Queue;

/**
 * Created by maxim on 30.10.18.
 */
public class ChatContentController {

    private ChatWindowController parent = null;

    @FXML
    private VBox chatArea;

    @FXML
    private TextArea textArea;

    @FXML
    private ScrollPane scrollPane;

    private String name;

    private AccountWebSocketEndpoint socketEndpoint;

    @FXML
    public void initialize(){
        chatArea.prefWidthProperty().bind(scrollPane.widthProperty());
        scrollPane.vvalueProperty().bind(chatArea.heightProperty());

        textArea.setOnKeyPressed((KeyEvent event)->{
            if(event.getCode() == KeyCode.ENTER) {
                this.onSendButtonClicked();
                event.consume();
            }
        });
    }

    public void addMessages(String name, Queue<String> messages){
        this.name = name;
        if(messages != null){
            while (!messages.isEmpty()){
                Label messageLabel = new Label(name+": " + messages.poll());
                messageLabel.setWrapText(true);
                chatArea.getChildren().add(messageLabel);
            }
        }

        ((Stage)scrollPane.getScene().getWindow()).toFront();
    }

    public void addMessage(String name, String message){
        Label messageLabel = new Label(name+": " + message);
        messageLabel.setWrapText(true);
        chatArea.getChildren().add(messageLabel);
    }

    public void setSocketEndpoint(AccountWebSocketEndpoint socketEndpoint){
        this.socketEndpoint = socketEndpoint;
    }

    @FXML
    public void onSendButtonClicked(){
        if(!"".equals(textArea.getText())&&socketEndpoint!=null) {
            Label messageLabel = new Label("me: " + textArea.getText());
            messageLabel.setWrapText(true);
            chatArea.getChildren().add(messageLabel);

            socketEndpoint.sendMessage(name, textArea.getText());
        }

        textArea.setText("");
    }

    @FXML
    public void onCloseButtonClicked(){
        this.parent.closeTab(this.name);
    }

    public  void setParent(ChatWindowController parent){
        this.parent = parent;
    }
}
