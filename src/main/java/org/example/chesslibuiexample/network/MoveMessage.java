package org.example.chesslibuiexample.network;

public class MoveMessage {
    private String gameId;
    private String from;
    private String to;

    public MoveMessage() {}

    public MoveMessage(String gameId, String from, String to) {
        this.gameId = gameId;
        this.from = from;
        this.to = to;
    }

    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }
    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }
    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
}
