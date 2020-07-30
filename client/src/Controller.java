package client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    TextArea textArea;

    @FXML
    TextField msgField, loginField;

    @FXML
    HBox msgPanel, authPanel;

    @FXML
    PasswordField passField;

    @FXML
    ListView<String> clientsList;

    private Network network;
    private String nickname;

    public void setAuthenticated(boolean authenticated) {
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        msgPanel.setVisible(authenticated);
        msgPanel.setManaged(authenticated);
        clientsList.setVisible(authenticated);
        clientsList.setManaged(authenticated);
        if (!authenticated) {
            nickname = "";
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAuthenticated(false);
        clientsList.setOnMouseClicked(this::clientClickHandler);
        createNetwork();
        network.connect();
    }

    public void sendAuth() {
        network.sendAuth(loginField.getText(), passField.getText());
        loginField.clear();
        passField.clear();
    }

    public void sendMsg() {
        if (network.sendMsg(msgField.getText())) {
            msgField.clear();
            msgField.requestFocus();
        }
    }

    public void sendExit() {
        network.sendMsg("/end");
    }

    public void showAlert(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
            alert.showAndWait();
        });
    }

    public void createNetwork() {
        network = new Network();
        network.setCallOnException(args -> showAlert(args[0].toString()));

        network.setCallOnCloseConnection(args -> setAuthenticated(false));

        network.setCallOnAuthenticated(args -> {
            setAuthenticated(true);
            nickname = args[0].toString();
        });

        network.setCallOnMsgReceived(args -> {
            String msg = args[0].toString();
            if (msg.startsWith("/clients ")) {
                String[] tokens = msg.split("\\s");
                Platform.runLater(() -> {
                    clientsList.getItems().clear();
                    for (int i = 1; i < tokens.length; i++) {
                        if (!nickname.equals(tokens[i])) {
                            clientsList.getItems().add(tokens[i]);
                        }
                    }
                });
            } else if (msg.startsWith("/exception ")) {
                showAlert(msg.substring(11));
            } else {
                textArea.appendText(msg + "\n");
            }
        });
    }

    private void clientClickHandler(MouseEvent event) {
        if (event.getClickCount() == 2) {
            String nickname = clientsList.getSelectionModel().getSelectedItem();
            msgField.setText("/w " + nickname + " ");
            msgField.requestFocus();
            msgField.selectEnd();
        }
    }
}