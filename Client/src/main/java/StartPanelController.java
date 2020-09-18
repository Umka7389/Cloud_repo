import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class StartPanelController implements Initializable {

    @FXML
    AnchorPane anchorPane;
    @FXML
    TextField loginField;
    @FXML
    PasswordField passwordField;
    @FXML
    Button registrationButton;
    @FXML
    VBox registrationBlock;
    @FXML
    Button finalRegistrationButton;
    @FXML
    Button cancelRegistrationButton;
    @FXML
    TextField registrationLoginForm;
    @FXML
    TextField registrationPassForm;
    @FXML
    TextField repeatPassForm;
    @FXML
    Button entryButton;
    @FXML
    Label messageToUser;
    @FXML
    Label registrationMessage;
    @FXML
    Label registrationSuccessNotification;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ServerConnectionController.startConnection();
        StartPanelThread.setDaemon(true);
        StartPanelThread.start();
    }

    public void showRegistrationForms() {
        registrationBlock.setVisible(true);
        finalRegistrationButton.setVisible(true);
        registrationButton.setVisible(false);
        cancelRegistrationButton.setVisible(true);
        registrationSuccessNotification.setVisible(false);
    }

    public void cancelRegistration() {
        registrationButton.setVisible(true);
        cancelRegistrationButton.setVisible(false);
        registrationLoginForm.clear();
        registrationPassForm.clear();
        repeatPassForm.clear();
        registrationBlock.setVisible(false);
        finalRegistrationButton.setVisible(false);
        registrationMessage.setText("");
        registrationSuccessNotification.setVisible(false);

    }

    public void sendAuthMessage() {
        if (!loginField.getText().isEmpty() && !passwordField.getText().isEmpty()) {
            ServerConnectionController.sendAuthMessageToServer(loginField.getText(), passwordField.getText());
            loginField.clear();
            passwordField.clear();
        }
    }

    public void sendRegMessageToServer() {
        if (!registrationLoginForm.getText().isEmpty() && !registrationPassForm.getText().isEmpty() && !repeatPassForm.getText().isEmpty()) {
            if (registrationPassForm.getText().equals(repeatPassForm.getText())) {
                ServerConnectionController.sendRegMessageToServer(registrationLoginForm.getText(), repeatPassForm.getText());
            } else {
                registrationMessage.setText("You've entered unequal passwords");
                registrationPassForm.clear();
                repeatPassForm.clear();
            }
        }
    }

    public void authorizeAndSwitchToMainPanel(String login) {
        Platform.runLater(() -> {
            try {
                Stage stage;
                Parent root;
                stage = (Stage) entryButton.getScene().getWindow();
                root = FXMLLoader.load(getClass().getResource("/MainPanel.fxml"));
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setResizable(false);
                stage.setTitle("Cloud Storage" + File.separator + "" + login);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    Thread StartPanelThread = new Thread(this::run);

    private void run() {
        for (; ; ) {
            Object serverMessage = null;
            try {
                serverMessage = ServerConnectionController.readIncomingObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            if (serverMessage.toString().startsWith("userIsValid")) {
                String[] recieveLogin = serverMessage.toString().split("±");
                String login = recieveLogin[1];
                CurrentLogin.setCurrentLogin(login);
                authorizeAndSwitchToMainPanel(login);
            } else if (serverMessage.toString().startsWith("wrongPassword")) {
                Platform.runLater(() -> {
                    messageToUser.setText("Wrong password");
                });
            } else if (serverMessage.toString().startsWith("userDoesNotExist")) {
                Platform.runLater(() -> {
                    messageToUser.setText("Such user does not exist");
                    messageToUser.setLayoutX(567.5);
                });
            } else if (serverMessage.toString().equals("userAlreadyExists")) {
                Platform.runLater(() -> {
                    registrationMessage.setText("Such user already exists");
                    registrationLoginForm.clear();
                });

            } else if (serverMessage.toString().equals("userRegistered")) {
                Platform.runLater(() -> {
                    registrationBlock.setVisible(false);
                    registrationButton.setVisible(true);
                    cancelRegistrationButton.setVisible(false);
                    registrationSuccessNotification.setVisible(true);
                    loginField.setText(registrationLoginForm.getText());
                    passwordField.setText(repeatPassForm.getText());
                });
            }
        }
    }
}
