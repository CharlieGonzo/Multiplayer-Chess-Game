module org.example.chesslibuiexample {
    requires javafx.controls;
    requires javafx.fxml;
    requires chesslib;
    requires spring.messaging;
    requires spring.websocket;
    requires java.desktop;

    opens org.example.chesslibuiexample to javafx.fxml;
    exports org.example.chesslibuiexample;
    opens org.example.chesslibuiexample.controller to javafx.fxml;
    exports org.example.chesslibuiexample.network;
    opens org.example.chesslibuiexample.network to spring.messaging, javafx.fxml;

}