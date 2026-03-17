package org.example.chesslibuiexample.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.chesslibuiexample.HelloApplication;
import org.example.chesslibuiexample.network.ChessWebSocketClient;
import org.example.chesslibuiexample.network.MatchFoundMessage;

import java.io.IOException;

public class QueueController {

    @FXML private Label statusLabel;

    public void init(ChessWebSocketClient client, String username, Stage stage) {
        Platform.runLater(() -> statusLabel.setText("Waiting for opponent..."));

        client.joinQueue(username, (MatchFoundMessage msg) -> Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("board.fxml"));
                Scene scene = new Scene(loader.load(), 800, 600);
                BoardController boardController = loader.getController();
                boardController.initMultiplayer(client, msg.getGameId(), msg.getAssignedSide(), msg.getOpponentUsername());
                stage.setScene(scene);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
    }
}
