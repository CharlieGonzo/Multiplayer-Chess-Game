package org.example.chesslibuiexample.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.chesslibuiexample.HelloApplication;
import org.example.chesslibuiexample.network.ChessWebSocketClient;

import java.io.IOException;

public class UsernameController {

    @FXML private TextField usernameField;
    @FXML private Label errorLabel;

    public void onJoinQueue(ActionEvent event) throws IOException {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            errorLabel.setText("Please enter a username.");
            return;
        }

        errorLabel.setText("Connecting...");
        usernameField.setDisable(true);

        ChessWebSocketClient client = new ChessWebSocketClient();

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

        client.connect(() -> Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("QueueScreen.fxml"));
                Scene scene = new Scene(loader.load(), 400, 300);
                QueueController queueController = loader.getController();
                queueController.init(client, username, stage);
                stage.setScene(scene);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
    }
}
