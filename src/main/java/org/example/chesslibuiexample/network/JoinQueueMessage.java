package org.example.chesslibuiexample.network;

public class JoinQueueMessage {
    private String username;
    private String clientId;

    public JoinQueueMessage() {}

    public JoinQueueMessage(String username, String clientId) {
        this.username = username;
        this.clientId = clientId;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
}
