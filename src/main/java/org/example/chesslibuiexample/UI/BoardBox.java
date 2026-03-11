package org.example.chesslibuiexample.UI;

import com.github.bhlangonijr.chesslib.Square;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;

import java.util.Objects;

public class BoardBox extends StackPane {


    private Square square;

    Image pieceImage;

    public void setSquare(Square square) {
        this.square = square;
    }

    public Image getPieceImage() {
        return pieceImage;
    }

    public void setPieceImage(Image pieceImage) {
        this.pieceImage = pieceImage;
    }

    public BoardBox(Square square)
    {
        this.square = square;
    }

    public Square getSquare(){
        return this.square;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BoardBox boardBox = (BoardBox) o;
        return square == boardBox.square && Objects.equals(pieceImage, boardBox.pieceImage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(square, pieceImage);
    }
}
