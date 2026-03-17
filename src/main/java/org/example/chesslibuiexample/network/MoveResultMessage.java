package org.example.chesslibuiexample.network;

public class MoveResultMessage {
    private String from;
    private String to;
    private String nextSide;
    private String winCondition;

    public MoveResultMessage() {}

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }
    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
    public String getNextSide() { return nextSide; }
    public void setNextSide(String nextSide) { this.nextSide = nextSide; }
    public String getWinCondition() { return winCondition; }
    public void setWinCondition(String winCondition) { this.winCondition = winCondition; }
}
