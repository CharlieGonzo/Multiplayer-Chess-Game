package org.example.chesslibuiexample.network;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.UUID;
import java.util.function.Consumer;

public class ChessWebSocketClient {

    public static final String SERVER_URL = "ws://localhost:8080/chess-server";

    private StompSession session;
    private final String clientId = UUID.randomUUID().toString();

    public void connect(Runnable onConnected) {
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        stompClient.connectAsync(SERVER_URL, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession s, StompHeaders connectedHeaders) {
                session = s;
                onConnected.run();
            }
        });
    }

    public void joinQueue(String username, Consumer<MatchFoundMessage> onMatch) {
        session.subscribe("/topic/queue/" + clientId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return MatchFoundMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                onMatch.accept((MatchFoundMessage) payload);
            }
        });
        session.send("/app/queue", new JoinQueueMessage(username, clientId));
    }

    public void subscribeToGame(String gameId,
                                Consumer<MoveResultMessage> onMove,
                                Consumer<GameOverMessage> onGameOver) {
        session.subscribe("/topic/game/" + gameId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return MoveResultMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                MoveResultMessage msg = (MoveResultMessage) payload;
                if ("OPPONENT_DISCONNECTED".equals(msg.getWinCondition())) {
                    GameOverMessage gom = new GameOverMessage();
                    gom.setResult("OPPONENT_DISCONNECTED");
                    onGameOver.accept(gom);
                } else {
                    onMove.accept(msg);
                }
            }
        });
    }

    public void sendMove(String gameId, String from, String to) {
        if (session != null && session.isConnected()) {
            session.send("/app/game/move", new MoveMessage(gameId, from, to));
        }
    }

    public void disconnect() {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }
}
