package Shalev_Aviv.TRAFFI.WebSocket;

import org.springframework.lang.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final CarWebSocketHandler carWebSocketHandler;

    public WebSocketConfig(CarWebSocketHandler carWebSocketHandler) {
        this.carWebSocketHandler = carWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
        registry.addHandler(carWebSocketHandler, "/cars").setAllowedOrigins("*");
    }
}