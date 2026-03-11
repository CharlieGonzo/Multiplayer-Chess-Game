package org.example.chesslibuiexample.controller;

import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import org.example.chesslibuiexample.UI.BoardBox;
import org.example.chesslibuiexample.util.ChessLibAdapter;
import org.example.chesslibuiexample.util.PieceImageContainer;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class BoardController implements Initializable {

    @FXML
    GridPane board;

    ChessLibAdapter chessGame;

    PieceImageContainer container;

    private static final String[] cols = new String[]{"A","B","C","D","E","F","G","H","I"};


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chessGame = new ChessLibAdapter();
        container = new PieceImageContainer();
        createStartingBoard();
    }

    private void clickEventForBox(MouseEvent e){
        BoardBox clickedBox = (BoardBox)e.getSource();
        if(chessGame.getSelectionState()){
            BoardBox curr = (BoardBox)board.getChildren().stream().filter(e1 -> {BoardBox currSelected = (BoardBox) e1;return currSelected.getSquare().equals(chessGame.getSelectedSquare());}).findFirst().get(); // find the currently selected box
            if(chessGame.confirmMove(clickedBox.getSquare(),new Move(chessGame.getSelectedSquare(),clickedBox.getSquare()))){ // if move went through
                proccesBoxes(clickedBox,curr);
                chessGame.clearActions();// function
                if(chessGame.checkWin()){ // check for wins
                   getWinAlert().showAndWait();
                }
            }else{ // user clicked on non-valid move space, reset UI
                chessGame.clearActions();
                chessGame.seeMove(clickedBox.getSquare());
            }
        }else { // user clicked on a piece looking for
            chessGame.seeMove(clickedBox.getSquare());
        }
        redrawBoard();
    }

    private void proccesBoxes(BoardBox box1,BoardBox box2){
        box1.setPieceImage(box2.getPieceImage());
        box2.getChildren().clear();
        box2.setPieceImage(null);
        box1.getChildren().clear(); // if a piece was taken. take that piece out
        box1.getChildren().add(new ImageView(box1.getPieceImage()));
        chessGame.clearActions();// function
    }

    private Alert getWinAlert(){
        return new Alert(Alert.AlertType.INFORMATION,"Game is over");
    }

    private Node getNodeByRowColumnIndex(final int row, final int column, GridPane gridPane) {
        for (Node node : gridPane.getChildren()) {
            // Use static methods to get indices (default to 0 if null)
            Integer nodeRow = GridPane.getRowIndex(node);
            Integer nodeCol = GridPane.getColumnIndex(node);

            if ((nodeRow == null ? 0 : nodeRow) == row &&
                    (nodeCol == null ? 0 : nodeCol) == column) {
                return node;
            }
        }
        return null;
    }

    private boolean isValidSquare(BoardBox node,List<Move> possibleMoves){
        return possibleMoves.stream().anyMatch(e->e.getTo().equals(node.getSquare()) || e.getFrom().equals(node.getSquare()));
    }

    private void redrawBoard(){
        for (int col = 0; col < board.getColumnCount(); col++) {
            int count = col; // makes sure there is alternation patterns for the board
            for (int row = 0; row < board.getRowCount(); row++) {
                BoardBox node = (BoardBox)getNodeByRowColumnIndex(row, col, board);
                if(isValidSquare(node,chessGame.getPossibleMoves())) {
                    chessGame.setSelectionState(true);
                    if(count%2==0) {
                        node.setStyle("-fx-background-color: #c0fcc6; -fx-border-color: white; -fx-border-width: 4; -fx-border-style: solid;");
                        count++;
                    }else{
                        node.setStyle("-fx-background-color: #42f554; -fx-border-color: white; -fx-border-width: 4; -fx-border-style: solid;");
                        count++;
                    } // set white border on possible choices or the the one selected
                }else{
                    if(count%2==0) {
                        node.setStyle("-fx-background-color: #c0fcc6;");
                        count++;
                    }else{
                        node.setStyle("-fx-background-color: #42f554;");
                        count++;
                    }
                }
            }
        }
    } // end of method

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
        if(count%2==0) {
            newBox.setStyle("-fx-background-color: #c0fcc6;");
            count++;
        }else{
            newBox.setStyle("-fx-background-color: #42f554;");
            count++;
        }
        if(newBox.getPieceImage()!=null) { // means that it is one of the starting posisitons that need a piece
            newBox.getChildren().add(new ImageView(newBox.getPieceImage()));
        }
        newBox.setAlignment(Pos.CENTER);
        newBox.setOnMouseClicked(this::clickEventForBox);
        board.add(newBox,col,row);
        return count;
    }



}
