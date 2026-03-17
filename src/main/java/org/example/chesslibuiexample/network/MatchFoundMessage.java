package org.example.chesslibuiexample.network;

public class MatchFoundMessage {
    private String gameId;
    private String opponentUsername;
    private String assignedSide;

    public MatchFoundMessage() {}

    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }
    public String getOpponentUsername() { return opponentUsername; }
    public void setOpponentUsername(String opponentUsername) { this.opponentUsername = opponentUsername; }
    public String getAssignedSide() { return assignedSide; }
    public void setAssignedSide(String assignedSide) { this.assignedSide = assignedSide; }
}
