package org.example.chesslibuiexample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.chesslibuiexample.util.PieceImageContainer;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
//        PieceImageContainer container = new PieceImageContainer();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("board.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }
}
