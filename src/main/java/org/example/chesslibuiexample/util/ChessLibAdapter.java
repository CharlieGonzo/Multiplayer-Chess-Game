package org.example.chesslibuiexample.util;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;

public class ChessLibAdapter {

    Board board;

    Square selectedSquare;

    List<Move> possibleMoves;

    boolean selectionState = false;
    public ChessLibAdapter(Board board){
        this.board = board;
    }

    public  ChessLibAdapter() {
        this.board = new Board();
    }

    public boolean checkWin(){
        return board.isDraw() || board.isMated() || board.isStaleMate();
    }

    public void seeMove(Square square) {
        System.out.println(square);
//        System.out.println(board.legalMoves());
//        board.doMove(new Move(Square.E2,Square.E3));
//        System.out.println(board.legalMoves());
        List<Move> legalMoves = board.legalMoves().stream().filter(e->e.getFrom().equals(square)).toList();
        if(!legalMoves.isEmpty()){
            this.selectedSquare = square;
            System.out.println(selectedSquare);
            System.out.println(legalMoves);
            possibleMoves = legalMoves;
        }

    }

    public boolean confirmMove(Square square, Move move){
        System.out.println(move.getTo());
        if(getSelectionState()){
            if(possibleMoves.stream().anyMatch(e->{
                System.out.println(e.getTo());
                System.out.println(e.getFrom());
                return e.getTo().equals(move.getTo());
            })){
                board.doMove(move);
                System.out.println("move done");
                return true;
            }
        }
        return false;
    }

    public void clearActions(){
        selectionState = false;
        possibleMoves = new ArrayList<>();
    }

    public void revertMove() {


    }

    public void getTime(){

    }

    public List<Move> getPossibleMoves() {
        return possibleMoves;
    }

    public void setSelectionState(boolean selectionState) {
        this.selectionState = selectionState;
    }

    public boolean getSelectionState() {
        return selectionState;
    }

    public Square getSelectedSquare() {
        return selectedSquare;
    }
}
