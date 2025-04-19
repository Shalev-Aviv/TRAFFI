package Shalev_Aviv.TRAFFI.WebSocket;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CarWebSocketHandler extends TextWebSocketHandler {
    private final Set<WebSocketSession> sessions = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("Car WebSocket connection established: " + session.getId());
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        // No handling needed for incoming messages.
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("Car WebSocket connection closed: " + session.getId());
    }

    public void sendCarUpdate(int carId, int lane, String type) {
        String jsonMessage = String.format(
            "{\"carId\": %d, \"lane\": %d, \"type\": \"%s\"}",
            carId, lane, type
        );
        System.out.println("Sending car update: " + jsonMessage);
        sessions.forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(jsonMessage));
                }
            }
            catch (IOException e) {
                System.err.println("Error sending car update: " + e.getMessage());
            }
        });
    }
    public void sendCarLaneChange(int carId, int NextLane) {
        String jsonMessage = String.format(
            "{\"carId\": %d, \"NextLane\": %d}",
            carId, NextLane
        );
        System.out.println("Sending car lane change: " + jsonMessage);
        sessions.forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(jsonMessage));
                }
            }
            catch (IOException e) {
                System.err.println("Error sending car lane change: " + e.getMessage());
            }
        });
    }
}