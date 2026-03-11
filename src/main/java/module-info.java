module org.example.chesslibuiexample {
    requires javafx.controls;
    requires javafx.fxml;
    requires chesslib;


    opens org.example.chesslibuiexample to javafx.fxml;
    exports org.example.chesslibuiexample;
    // ADD THIS LINE:
    opens org.example.chesslibuiexample.controller to javafx.fxml;
}