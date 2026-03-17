package org.example.chesslibuiexample.controller;

import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.chesslibuiexample.HelloApplication;
import org.example.chesslibuiexample.UI.BoardBox;
import org.example.chesslibuiexample.model.WinCondition;
import org.example.chesslibuiexample.network.ChessWebSocketClient;
import org.example.chesslibuiexample.network.GameOverMessage;
import org.example.chesslibuiexample.network.MoveResultMessage;
import org.example.chesslibuiexample.util.ChessLibAdapter;
import org.example.chesslibuiexample.util.PieceImageContainer;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class BoardController implements Initializable {

    @FXML
    GridPane board;

    @FXML
    private HBox bottomMenu;

    @FXML
    private Label sideturn;

    @FXML
    private Label blackTimer;

    @FXML
    private Label whiteTimer;

    private static final int START_SECONDS = 30 * 60;

    private int blackSeconds = START_SECONDS;

    private int whiteSeconds = START_SECONDS;

    private Timeline timer;

    ChessLibAdapter chessGame;

    PieceImageContainer container;

    private static final String[] cols = new String[]{"A","B","C","D","E","F","G","H","I"};

    // Multiplayer state
    private boolean multiplayerMode = false;
    private String assignedSide;
    private ChessWebSocketClient wsClient;
    private String gameId;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chessGame = new ChessLibAdapter();
        container = new PieceImageContainer();
        sideturn.setText(chessGame.getCurrentSide()+"'s turn");
        createStartingBoard();
        startUpTimers();
    }

    public void initMultiplayer(ChessWebSocketClient client, String gameId, String assignedSide, String opponentUsername) {
        this.multiplayerMode = true;
        this.wsClient = client;
        this.gameId = gameId;
        this.assignedSide = assignedSide;
        sideturn.setText("You are " + assignedSide + " vs " + opponentUsername);

        client.subscribeToGame(gameId, this::onRemoteMove, this::onGameOver);
    }

    private void onRemoteMove(MoveResultMessage msg) {
        Platform.runLater(() -> {
            Square from = Square.valueOf(msg.getFrom());
            Square to = Square.valueOf(msg.getTo());

            BoardBox fromBox = getBoardBoxBySquare(from);
            BoardBox toBox = getBoardBoxBySquare(to);
            if (fromBox != null && toBox != null) {
                proccesBoxes(toBox, fromBox);
            }

            chessGame.doMoveIfLegal(from, to);
            sideturn.setText(msg.getNextSide() + "'s turn");
            redrawBoard();

            if (!"NONE".equals(msg.getWinCondition())) {
                handleMultiplayerGameOver(msg.getWinCondition());
            }
        });
    }

    private void onGameOver(GameOverMessage msg) {
        Platform.runLater(() -> handleMultiplayerGameOver(msg.getResult()));
    }

    private void handleMultiplayerGameOver(String result) {
        timer.stop();
        wsClient.disconnect();

        String message = switch (result) {
            case "WHITE_WIN" -> "WHITE wins!";
            case "BLACK_WIN" -> "BLACK wins!";
            case "DRAW", "STALEMATE" -> "Draw!";
            case "OPPONENT_DISCONNECTED" -> "Opponent disconnected. You win!";
            default -> "Game over.";
        };

        getWinAlert(message).showAndWait();
        returnToStartScreen();
    }

    private void returnToStartScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("Start.fxml"));
            Scene scene = new Scene(loader.load(), 400, 300);
            Stage stage = (Stage) board.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            System.exit(0);
        }
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void startUpTimers() {
        whiteTimer.setText("White: " + formatTime(whiteSeconds));
        blackTimer.setText("Black: " + formatTime(blackSeconds));

        timer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            switch (chessGame.getCurrentSide()) {
                case "BLACK" -> {
                    blackSeconds--;
                    blackTimer.setText("Black: " + formatTime(blackSeconds));
                    if (blackSeconds <= 0) {
                        timer.stop();
                        Platform.runLater(() -> {
                            getWinAlert("Time's up! White wins!!!").showAndWait();
                            if (multiplayerMode) returnToStartScreen(); else System.exit(0);
                        });
                    }
                }
                case "WHITE" -> {
                    whiteSeconds--;
                    whiteTimer.setText("White: " + formatTime(whiteSeconds));
                    if (whiteSeconds <= 0) {
                        timer.stop();
                        Platform.runLater(() -> {
                            getWinAlert("Time's up! Black wins!!!").showAndWait();
                            if (multiplayerMode) returnToStartScreen(); else System.exit(0);
                        });
                    }
                }
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void clickEventForBox(MouseEvent e){
        if (multiplayerMode && !chessGame.getCurrentSide().equals(assignedSide)) {
            return; // not your turn
        }

        BoardBox clickedBox = (BoardBox)e.getSource();
        if(chessGame.getSelectionState()){
            BoardBox curr = getCurrentlySelectedBoardBox();
            if (multiplayerMode) {
                // validate locally for highlights, then send to server
                if (chessGame.getPossibleMoves().stream().anyMatch(m -> m.getTo().equals(clickedBox.getSquare()))) {
                    wsClient.sendMove(gameId, curr.getSquare().name(), clickedBox.getSquare().name());
                    chessGame.clearActions();
                } else {
                    chessGame.clearActions();
                    chessGame.seeMove(clickedBox.getSquare());
                }
            } else {
                if(confirmMove(clickedBox.getSquare())){
                    processConfirmedMove(clickedBox,curr);
                }else{
                    chessGame.clearActions();
                    chessGame.seeMove(clickedBox.getSquare());
                }
            }
        }else {
            chessGame.seeMove(clickedBox.getSquare());
        }
        redrawBoard();
    }

    private boolean confirmMove(Square clickSquare){
        return chessGame.confirmMove(clickSquare,new Move(chessGame.getSelectedSquare(),clickSquare));
    }

    private void proccesBoxes(BoardBox box1,BoardBox box2){
        box1.setPieceImage(box2.getPieceImage());
        box2.getChildren().clear();
        box2.setPieceImage(null);
        box1.getChildren().clear();
        box1.getChildren().add(new ImageView(box1.getPieceImage()));
        chessGame.clearActions();
    }

    private Alert getWinAlert(String message){
        return new Alert(Alert.AlertType.INFORMATION,message);
    }

    private void checkForWin(){
        switch (chessGame.checkWin()){
            case BLACK_WIN ->  {
                timer.stop();
                getWinAlert("Game Over. Black wins!!!").showAndWait();
                System.exit(0);
            }
            case WHITE_WIN -> {
                timer.stop();
                getWinAlert("Game over. White wins!!!").showAndWait();
                System.exit(0);
            }
            case DRAW -> {
                timer.stop();
                getWinAlert("Draw").showAndWait();
                System.exit(0);
            }
        }
    }

    private Node getNodeByRowColumnIndex(final int row, final int column, GridPane gridPane) {
        for (Node node : gridPane.getChildren()) {
            Integer nodeRow = GridPane.getRowIndex(node);
            Integer nodeCol = GridPane.getColumnIndex(node);
            if ((nodeRow == null ? 0 : nodeRow) == row &&
                    (nodeCol == null ? 0 : nodeCol) == column) {
                return node;
            }
        }
        return null;
    }

    private BoardBox getBoardBoxBySquare(Square square) {
        return (BoardBox) board.getChildren().stream()
                .filter(n -> ((BoardBox) n).getSquare().equals(square))
                .findFirst()
                .orElse(null);
    }

    private boolean isValidSquare(BoardBox node,List<Move> possibleMoves){
        return possibleMoves.stream().anyMatch(e->e.getTo().equals(node.getSquare()) || e.getFrom().equals(node.getSquare()));
    }

    private void processConfirmedMove(BoardBox clickedBox,BoardBox curr){
        sideturn.setText(chessGame.getCurrentSide()+"'s turn");
        proccesBoxes(clickedBox,curr);
        chessGame.clearActions();
        checkForWin();
    }

    private void styleSelectedSquare(int count,Node node){
            node.setStyle(count % 2 == 0 ? "-fx-background-color: #c0fcc6; -fx-border-color: white; -fx-border-width: 4; -fx-border-style: solid;" : "-fx-background-color: #42f554; -fx-border-color: white; -fx-border-width: 4; -fx-border-style: solid;");
    }

    private void styleNonSelectedSquare(int count,Node node){
        if(count%2==0) {
            node.setStyle("-fx-background-color: #c0fcc6;");
        }else {
            node.setStyle("-fx-background-color: #42f554;");
        }
    }

    private void redrawBoard(){
        for (int col = 0; col < board.getColumnCount(); col++) {
            int count = col;
            for (int row = 0; row < board.getRowCount(); row++) {
                BoardBox node = (BoardBox)getNodeByRowColumnIndex(row, col, board);
                redrawSquare(count,node);
                count++;
            }
        }
    }

    private void redrawSquare(int count,Node node){
        if(isValidSquare((BoardBox) node,chessGame.getPossibleMoves())) {
            chessGame.setSelectionState(true);
            styleSelectedSquare(count,node);
            return;
        }
            styleNonSelectedSquare(count,node);

    }

    private void createStartingBoard(){
        for (int col = 0; col < board.getColumnCount(); col++) {
            int count = col;
            for (int row = 0; row < board.getRowCount(); row++) {
                Node node = getNodeByRowColumnIndex(row, col, board);
                if (node == null) {
                    int currRowOnBoard = row+1;
                   count = generateBox(cols[col] + currRowOnBoard,count,col,row);
                }
            }
        }
    }

    private int generateBox(String square,int count,int col,int row){
        BoardBox newBox = new BoardBox(Square.valueOf(square));
        newBox.setPieceImage(container.getImages().get(newBox.getSquare().toString().toLowerCase()));
        styleNonSelectedSquare(count++,newBox);
        if(newBox.getPieceImage()!=null) {
            newBox.getChildren().add(new ImageView(newBox.getPieceImage()));
        }
        newBox.setAlignment(Pos.CENTER);
        newBox.setOnMouseClicked(this::clickEventForBox);
        board.add(newBox,col,row);
        return count;
    }

    private BoardBox getCurrentlySelectedBoardBox(){
        return (BoardBox)board.getChildren().stream()
                .filter(e1 -> {
                    BoardBox currSelected = (BoardBox) e1;
                    return currSelected.getSquare().equals(chessGame.getSelectedSquare());
                })
                .findFirst()
                .get();
    }
}
