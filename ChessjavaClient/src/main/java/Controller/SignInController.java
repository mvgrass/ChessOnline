package Controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import requests.RequestException;
import requests.RequestAPI;
import requests.RequestApiImpl;
import requests.WrongParameterException;

import java.io.*;

/**
 * Created by maxim on 13.10.18.
 */
public class SignInController implements Unresizable{

    private RequestAPI requestAPI;

    @FXML
    public void initialize(){
        readSettings();
        requestAPI = RequestApiImpl.getInstance();
    }

    @FXML
    private Button signInButton;

    @FXML
    private Button signUpButton;

    @FXML
    private TextField login;

    @FXML
    private PasswordField password;

    @FXML
    private Label errorLabel;

    @FXML
    private CheckBox rememberBox;

    @FXML
    public void onSignInClicked(){

        final String login = this.login.getText();
        final String password = this.password.getText();

        if("".equals(login) || "".equals(password)){
            errorLabel.setText("Заполните поля \"login\" и \"password\"");
            return;
        }

        errorLabel.setText("");

        new Thread(()->{
            signInButton.setDisable(true);
            signUpButton.setDisable(true);
            try {
                requestAPI.signIn(login, password);
                saveSettings();
                Platform.runLater(()->{
                    SceneController.getInstance().activate("mainWindow");
                });

            }catch (RequestException exc){
                Platform.runLater(()->{
                    errorLabel.setText("Невозможно подключиться к серверу");});
            }catch(WrongParameterException exc) {
                Platform.runLater(()->{
                    errorLabel.setText("Неверное имя пользователя или пароль");
                });
            }finally {
                signInButton.setDisable(false);
                signUpButton.setDisable(false);
            }
        }).start();

    }

    @FXML
    public void onSignUpClicked(){
        final String login = this.login.getText();
        final String password = this.password.getText();

        if("".equals(login) || "".equals(password)){
            errorLabel.setText("Заполните поля \"login\" и \"password\"");
            return;
        }

        signInButton.setDisable(true);
        signUpButton.setDisable(true);

        errorLabel.setText("");

        new Thread(()->{
            try {
                requestAPI.signUp(login, password);
                saveSettings();
                Platform.runLater(()->{
                    SceneController.getInstance().activate("mainWindow");
                });
            }catch (RequestException exc){
                Platform.runLater(()->{
                    errorLabel.setText("Невозможно подкючиться к серверу");
                });
            }catch (WrongParameterException exc){
                Platform.runLater(()->{
                    errorLabel.setText("Данное имя пользователя уже занято");
                });
            }finally {
                signInButton.setDisable(false);
                signUpButton.setDisable(false);
            }
        }).start();
    }

    @Override
    public void setSize() {
        Stage mainStage = (Stage)errorLabel.getScene().getWindow();
        mainStage.setMinWidth(600);
        mainStage.setMaxWidth(600);
        mainStage.setMinHeight(400);
        mainStage.setMaxHeight(400);
        mainStage.setWidth(600);
        mainStage.setHeight(400);

        mainStage.setOnCloseRequest(windowEvent -> {

        });
    }

    private void saveSettings() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("settings"));){

            writer.write("host ");
            writer.write(RequestApiImpl.getInstance().getHost());
            writer.newLine();

            if(rememberBox.isSelected()){
                writer.write("login ");
                writer.write(login.getText());
                writer.newLine();

                writer.write("password ");
                writer.write(password.getText());
            }
        }catch (IOException exc){
            exc.printStackTrace();
        }
    }

    private void readSettings(){
        try (BufferedReader reader = new BufferedReader(new FileReader("settings"));){


            String line;
            do {
                line = reader.readLine();
                int index = -1;

                if(line!=null)
                    index= line.indexOf(' ');

                if (index>0) {
                    String key = line.substring(0, index);
                    String value = line.substring(index+1, line.length());


                    if("login".equals(key)){
                        login.setText(value);
                        rememberBox.setSelected(true);
                    }else if("password".equals(key)){
                        password.setText(value);
                    }else if("host".equals(key)){
                        RequestApiImpl.getInstance().setHost(value);
                    }
                }
            }while (line!=null);

        }catch (FileNotFoundException exc){
            exc.printStackTrace();
        }
        catch (IOException exc){
            exc.printStackTrace();
        }
    }
}
