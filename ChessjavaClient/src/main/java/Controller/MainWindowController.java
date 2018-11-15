package Controller;


import Model.Account;
import Model.Pieces.Color;
import Model.UserFriend;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import requests.*;

import javax.websocket.DeploymentException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Created by maxim on 13.10.18.
 */
public class MainWindowController implements Unresizable {

    private ChatWindowController chatController = null;

    private RequestAPI requestAPI;

    private AccountWebSocketEndpoint accountWebSocketEndpoint;

    private Map<String, GameController> games = new HashMap<>();

    private Map<String, Tab> gameTabs = new HashMap<>();

    private Reconnecter reconnecter;

    private Set<String> openedChats = new HashSet<>();

    private ObservableList<UserFriend> friends = FXCollections.observableArrayList();

    @FXML
    public ListView<UserFriend> friendsView;

    private ObservableList<String> invites = FXCollections.observableArrayList();

    @FXML
    public ListView<String> invitesView;

    @FXML
    private AnchorPane leftNode;

    @FXML
    private SplitPane splitPane;

    @FXML
    private TabPane gamesPane;

    @FXML
    public Label accountName;

    @FXML
    volatile public Label statusLabel;

    @FXML
    public void initialize(){
        requestAPI = RequestApiImpl.getInstance();
        leftNode.maxWidthProperty().bind(splitPane.widthProperty().multiply(0.2));

        gamesPane.getStylesheets().add("/css/mystyle.css");

        try {

            initializeFriends();

            friendsView.setCellFactory(new Callback<ListView<UserFriend>, ListCell<UserFriend>>() {
                @Override
                public ListCell<UserFriend> call(ListView<UserFriend> userFriendListView) {
                    ListCell<UserFriend> cell = new ListCell<UserFriend>(){

                        @Override
                        protected void updateItem(UserFriend userFriend, boolean b) {
                            super.updateItem(userFriend, b);
                            if(userFriend!=null) {
                                Image img = userFriend.getImage();
                                ImageView imageView = new ImageView(img);
                                setGraphic(imageView);
                                setText(userFriend.getName()+
                                        ((userFriend.getMessages().isEmpty())?"":" ("+userFriend.getMessages().size()+")"));
                            }else{
                                setGraphic(null);
                                setText("");
                            }
                        }
                    };

                    return cell;
                }
            });

            friendsView.setItems(friends);

            invitesView.setItems(invites);

            accountWebSocketEndpoint = new AccountWebSocketEndpoint(requestAPI.getHost(), requestAPI.getToken(), this);


        }catch (Exception exc){
            exc.printStackTrace();
            SceneController.getInstance().activate("signInWindow");
            throw new RuntimeException();
        }

        ContextMenu menu = new ContextMenu();
        MenuItem addFriend = new MenuItem("Add");
        MenuItem invite = new MenuItem("Invite..");
        MenuItem sendMessage = new MenuItem("Chat..");
        MenuItem delete = new MenuItem("Remove");

        addFriend.setOnAction((ActionEvent event)->{
            addFriend(friendsView.getFocusModel().getFocusedItem().getName());
        });

        invite.setOnAction((ActionEvent event) ->{
            ChoiceDialog<String> dialog = new ChoiceDialog<>();
            dialog.getItems().add("5");
            dialog.getItems().add("20");
            dialog.getItems().add("40");
            dialog.setTitle("Invite");
            dialog.setContentText("Choose duration of match in minutes: ");
            final Optional<String> duration = dialog.showAndWait();
            String durationImMillies = String.valueOf(Long.valueOf(duration.get())*60*1000);

            if(accountWebSocketEndpoint!=null){
                UserFriend focused = friendsView.getFocusModel().getFocusedItem();
                accountWebSocketEndpoint.sendInvite(focused.getName(), durationImMillies);
            }
        });

        sendMessage.setOnAction((ActionEvent)->{
            try {

                if (chatController == null) {
                    String fxmlFile = "/fxml/ChatWindow.fxml";
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                    Pane root = (Pane) loader.load();

                    Scene chatScene = new Scene(root);

                    chatController = (ChatWindowController) loader.getController();

                    chatController.setParent(this);
                    chatController.setSocketEndpoint(this.accountWebSocketEndpoint);

                    Stage chatStage = new Stage();
                    chatStage.setTitle("Chat");
                    chatStage.setScene(chatScene);
                    chatStage.initModality(Modality.NONE);

                    chatStage.setOnCloseRequest((WindowEvent)->{
                        this.chatController = null;
                        this.openedChats.clear();
                    });

                    chatStage.show();
                }

                chatController.addChat(friendsView.getFocusModel().getFocusedItem().getName(),
                        friendsView.getFocusModel().getFocusedItem().getMessages());

                openedChats.add(friendsView.getFocusModel().getFocusedItem().getName());

                forceListRefreshOn(friendsView);
            }catch (IOException exc){
                exc.printStackTrace();
            }
        });

        delete.setOnAction((ActionEvent event)->{
            UserFriend focusedFriend = friendsView.getFocusModel().getFocusedItem();

            if(focusedFriend.getStatus() == UserFriend.Status.ONLINE
                    ||focusedFriend.getStatus() == UserFriend.Status.OFFLINE)
                deleteFriend(focusedFriend.getName());
            else
                deleteFriendRequest(focusedFriend.getName());
        });

        menu.setOnShown(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
               FocusModel<UserFriend> focusModel = friendsView.getFocusModel();
               if(focusModel.getFocusedIndex()<0){
                   addFriend.setDisable(true);
                   invite.setDisable(true);
                   sendMessage.setDisable(true);
                   delete.setDisable(true);
               }else if(focusModel.getFocusedItem().getStatus() == UserFriend.Status.ONLINE){
                   addFriend.setDisable(true);
                   invite.setDisable(false);
                   sendMessage.setDisable(false);
                   delete.setDisable(false);
               } else if(focusModel.getFocusedItem().getStatus() == UserFriend.Status.OFFLINE){
                   addFriend.setDisable(true);
                   invite.setDisable(true);
                   sendMessage.setDisable(false);
                   delete.setDisable(false);
               }else if(focusModel.getFocusedItem().getStatus() == UserFriend.Status.INREQUEST){
                   addFriend.setDisable(false);
                   invite.setDisable(true);
                   sendMessage.setDisable(true);
                   delete.setDisable(false);
               }else {
                   addFriend.setDisable(true);
                   invite.setDisable(true);
                   sendMessage.setDisable(true);
                   delete.setDisable(false);
               }
           }
        });

        menu.getItems().addAll(invite, sendMessage,addFriend, delete);
        friendsView.setContextMenu(menu);

        ContextMenu invitesMenu = new ContextMenu();
        MenuItem accept = new MenuItem("Accept");
        MenuItem deny = new MenuItem("Deny");

        invitesMenu.setOnShown(windowEvent -> {
            if(invitesView.getFocusModel().getFocusedIndex()<0){
                accept.setDisable(true);
                deny.setDisable(true);
            }else{
                accept.setDisable(false);
                deny.setDisable(false);
            }
        });

        deny.setOnAction(event -> {
            invites.remove(invitesView.getFocusModel().getFocusedIndex());
        });

        accept.setOnAction(event -> {
            if(accountWebSocketEndpoint!=null){
                accountWebSocketEndpoint.acceptInvite(invitesView.getFocusModel().getFocusedItem());
                invites.remove(invitesView.getFocusModel().getFocusedIndex());
            }
        });

        invitesMenu.getItems().addAll(accept, deny);

        invitesView.setContextMenu(invitesMenu);


    }

    public void setSize(){
        Stage mainStage = (Stage)splitPane.getScene().getWindow();
        mainStage.setMinHeight(600);
        mainStage.setMinWidth(900);
        mainStage.setMaxHeight(600);
        mainStage.setMaxWidth(900);
        mainStage.setWidth(900);
        mainStage.setHeight(600);
        mainStage.setOnCloseRequest(windowEvent -> {
            windowEvent.consume();
            this.onExitClicked();
        });
    }

    @FXML
    public void onChangeAccountClicked(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Changing account");
        alert.setContentText("Are you sure you want to change account?\nAll unfinished parties will be lost!");
        Optional<ButtonType> choice = alert.showAndWait();

        if(choice.get() == ButtonType.OK) {
            if(accountWebSocketEndpoint!=null) {
                games.forEach((String gameId, GameController controller)->{
                    accountWebSocketEndpoint.sendGiveUp(gameId);
                });

                try {
                    Thread.sleep(500);
                }
                catch(InterruptedException exc){
                    exc.printStackTrace();
                }

                accountWebSocketEndpoint.close();
            }

            if(reconnecter!=null)
                reconnecter.interrupt();

            if(chatController!=null)
                chatController.close();
            SceneController.getInstance().activate("signInWindow");
        }
    }

    @FXML
    public void onExitClicked(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Exit");
        alert.setContentText("Are you sure you want to exit?\nAll unfinished parties will be lost!");
        Optional<ButtonType> choice = alert.showAndWait();

        if (choice.get() == ButtonType.OK) {
            if(accountWebSocketEndpoint!=null) {
                games.forEach((String gameId, GameController controller)->{
                    accountWebSocketEndpoint.sendGiveUp(gameId);
                });
                try {
                    Thread.sleep(500);
                }
                catch(InterruptedException exc){
                    exc.printStackTrace();
                }
                accountWebSocketEndpoint.close();
            }

            Platform.exit();
        }
    }

    @FXML
    public void onAddFriendClicked(){
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add friend");
        dialog.setContentText("Enter player name: ");
        final Optional<String> name = dialog.showAndWait();

        addFriend(name.get());
    }

    private void addFriend(String name){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    requestAPI.addFriend(name);
                    Platform.runLater(()->{
                        int index = findFriendByName(name);
                        if(index < 0){
                            friends.add(new UserFriend(name, UserFriend.Status.UNCONFIRMED));

                        }else{
                            friends.get(index).setStatus(UserFriend.Status.OFFLINE);
                        }
                    });

                }catch (RequestException exc){
                    exc.printStackTrace();
                }catch (WrongParameterException exc){
                    Platform.runLater(()->{
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setContentText("Can't add "+name+"!\nPlayer is not exists or you have been already added.");
                        alert.showAndWait();
                    });
                }
                catch (UnauthorizedException exc){
                    Platform.runLater(()->{
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setContentText("You are not authorized!\nPlease try to relogin to the game.");
                        alert.showAndWait();
                    });
                }



            }
        }).start();
    }

    private void deleteFriend(String name){
        new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                requestAPI.deleteFriend(name);
                Platform.runLater(()->{
                    onRemovingFriend(name);
                });

            }catch (RequestException exc){
                exc.printStackTrace();
            }catch (WrongParameterException exc){
                Platform.runLater(()->{
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setContentText("Can't remove "+name+"!\nPlayer is not exists or you have been already removed.");
                    alert.showAndWait();
                });
            }
            catch (UnauthorizedException exc){
                Platform.runLater(()->{
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setContentText("You are not authorized!\nPlease try to relogin to the game.");
                    alert.showAndWait();
                });
            }



        }
    }).start();}

    private void deleteFriendRequest(String name){
        new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                requestAPI.deleteFriendRequest(name);
                Platform.runLater(()->{
                    int index = findFriendByName(name);
                    friends.remove(index);
                });

            }catch (RequestException exc){
                exc.printStackTrace();
            }catch (WrongParameterException exc){
                Platform.runLater(()->{
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setContentText("Can't remove " + name + "!\nPlayer is not exists or you have been already removed.");
                    alert.showAndWait();
                });
            }
            catch (UnauthorizedException exc){
                Platform.runLater(()->{
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setContentText("You are not authorized!\nPlease try to relogin to the game.");
                    alert.showAndWait();
                });
            }



        }
    }).start();}

    public void chatDialogClosed(String name){
        openedChats.remove(name);
    }

    private int findFriendByName(String name){
        int index = -1;
        for(int i = 0; i<friends.size(); ++i){
            if(friends.get(i).getName().equals(name)){
                index = i;
                break;
            }
        }

        return index;
    }

    private int findInviteByName(String name){
        int index = -1;
        for(int i = 0; i<invites.size(); ++i){
            if(invites.get(i).equals(name)){
                index = i;
                break;
            }
        }

        return index;
    }

    public void onFriendOnline(String name){
        int index = findFriendByName(name);
        if(index >= 0){
            friends.get(index).setStatus(UserFriend.Status.ONLINE);
        }
        friends.sort(Comparator.naturalOrder());
    }

    public void onFriendOffline(String name){
        int index = findFriendByName(name);
        if(index >= 0){
            friends.get(index).setStatus(UserFriend.Status.OFFLINE);
        }
        friends.sort(Comparator.naturalOrder());
    }

    public void onAddingFriend(String name){
        int index = findFriendByName(name);
        if(index < 0){
            friends.add(new UserFriend(name, UserFriend.Status.INREQUEST));

        }else{
            friends.get(index).setStatus(UserFriend.Status.OFFLINE);
        }
        friends.sort(Comparator.naturalOrder());
    }

    public void onRemovingFriend(String name) {
        int index = findFriendByName(name);
        if (index >= 0) {
            friends.remove(index);
        }
    }

    public void onMessage(String from, String message){
        if(openedChats.contains(from)) {
            chatController.addMessage(from, message);
        } else {
            int index = findFriendByName(from);
            if(index>=0)
                friends.get(index).addMessage(message);

            forceListRefreshOn(friendsView);
        }
    }

    public void onInvite(String from){
        int index = findInviteByName(from);
        if(index < 0){
            invites.add(from);
        }
    }

    public void onRemoveInvite(String from){
        int index = findInviteByName(from);
        if(index>=0)
            invites.remove(index);
    }

    private void initializeFriends() throws RequestException, UnauthorizedException{
        Account account = requestAPI.getMyAccountInfo();

        accountName.setText(account.getLogin()+":");

        friends.clear();

        for(String friend:account.getFriends())
            friends.add(new UserFriend(friend, UserFriend.Status.OFFLINE));

        for(String friendRequest : account.getInRequests())
            friends.add(new UserFriend(friendRequest, UserFriend.Status.INREQUEST));

        for(String friendRequest : account.getOutRequests())
            friends.add(new UserFriend(friendRequest, UserFriend.Status.UNCONFIRMED));

        friends.sort(Comparator.naturalOrder());
    }

    public void reconnectToServer(){
        friends.stream()
                .filter(user->{return user.getStatus() == UserFriend.Status.ONLINE;})
                .forEach(user->{user.setStatus(UserFriend.Status.OFFLINE);});

        friends.sort(Comparator.naturalOrder());

        accountWebSocketEndpoint = null;
        if(chatController!=null)
            chatController.setSocketEndpoint(null);

        games.forEach((String key, GameController controller)->{
            controller.setEndpoint(null);
        });

        reconnecter = new Reconnecter(this);
        reconnecter.setDaemon(true);
        reconnecter.start();

    }

    private <T> void forceListRefreshOn(ListView<T> lsv) {
        ObservableList<T> items = lsv.<T>getItems();
        lsv.<T>setItems(null);
        lsv.<T>setItems(items);
    }

    public void addNewTab(String against, String color, String gameId, Long duration) {
        Tab tab = new Tab(against);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/GameContent.fxml"));

        try {
            Pane pane = (Pane) loader.load();

            GameController controller = (GameController) loader.getController();
            controller.setGameId(gameId);
            controller.setParent(this);
            controller.setEndpoint(accountWebSocketEndpoint);
            games.put(gameId, controller);

            controller.initGame(duration, "Dark".equals(color));


            tab.setContent(pane);
            gamesPane.getTabs().add(gamesPane.getTabs().size(), tab);
            gameTabs.put(gameId, tab);
            if(!tab.isSelected()&&!tab.getStyleClass().contains("updated")){
                tab.getStyleClass().add("updated");
            }

            tab.setOnSelectionChanged(event -> {
                if(tab.isSelected()){
                    tab.getStyleClass().remove("updated");
                }
            });
        }catch (IOException exc){
            exc.printStackTrace();
        }
    }

    public void updateGame(String gameId, String turn, List<String> desk,
                           String lastMove,
                           Long lightsTimer, Long darksTimer){
        GameController controller = games.get(gameId);
        if(controller!=null){
            controller.setTurn("Light".equals(turn)? Color.LIGHT: Color.DARK);
            controller.addLastMove(lastMove, "Light".equals(turn)? Color.DARK: Color.LIGHT);
            controller.setTime(lightsTimer, darksTimer);
            controller.updateBoard(desk);

            Tab tab = gameTabs.get(gameId);
            if(!tab.isSelected()&&!tab.getStyleClass().contains("updated")){
                tab.getStyleClass().add("updated");
            }
        }
    }

    public void endOfGame(String gameId, String winner, String reason){
        GameController controller = games.get(gameId);

        if(controller!=null){
            controller.endOfGame(winner, reason);
        }

        Tab tab = gameTabs.get(gameId);
        if(tab!=null&&!tab.isSelected()&&!tab.getStyleClass().contains("updated")){
            tab.getStyleClass().add("updated");
        }
    }

    public void closeGame(String gameId){
        GameController controller = games.remove(gameId);
        if(controller!=null)
            controller.onGiveUpButtonClicked();

        gamesPane.getTabs().remove(gameTabs.remove(gameId));
    }

    class Reconnecter extends Thread{
        MainWindowController controller;

        Reconnecter(MainWindowController controller){
            this.controller = controller;
        }

        public void run() {

            try {

                while (!isInterrupted()) {
                    Thread.sleep(5000);
                    try {
                        accountWebSocketEndpoint = new AccountWebSocketEndpoint(requestAPI.getHost(), requestAPI.getToken(), controller);

                        if (chatController != null) {
                            chatController.setSocketEndpoint(accountWebSocketEndpoint);
                        }

                        games.forEach((String key, GameController controller) -> {
                            controller.setEndpoint(accountWebSocketEndpoint);
                            accountWebSocketEndpoint.sendGetUpdate(controller.getGameId());
                        });

                        Platform.runLater(() -> {
                            try {
                                initializeFriends();
                            } catch (UnauthorizedException | RequestException exc) {
                                SceneController.getInstance().activate("signInWindow");
                            }
                        });

                        reconnecter = null;

                        break;
                    } catch (IOException | URISyntaxException | RuntimeException | DeploymentException exc) {
                        exc.printStackTrace();
                    }
                }

            }catch (InterruptedException exc){
                exc.printStackTrace();
            }
        }
    }
}
