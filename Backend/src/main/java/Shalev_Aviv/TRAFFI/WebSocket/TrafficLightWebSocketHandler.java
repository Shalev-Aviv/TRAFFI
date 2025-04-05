package Shalev_Aviv.TRAFFI.WebSocket;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TrafficLightWebSocketHandler extends TextWebSocketHandler {
    private final Set<WebSocketSession> sessions = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("Traffic light WebSocket connection established: " + session.getId());
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        // No handling needed for incoming messages
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("Traffic light WebSocket connection closed: " + session.getId());
    }

    public void sendTrafficLightUpdate(int lightId, boolean isGreen) {
        String status = isGreen ? "GREEN" : "RED";
        String jsonMessage = "{\"lightId\": " + lightId + ", \"status\": \"" + status + "\"}";
        
        System.out.println("Sending traffic light update: " + jsonMessage);
        
        sessions.forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(jsonMessage));
                }
            } catch (IOException e) {
                System.err.println("Error sending traffic light update: " + e.getMessage());
            }
        });
    }
}