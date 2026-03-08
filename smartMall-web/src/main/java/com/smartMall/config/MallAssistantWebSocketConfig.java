package com.smartMall.config;

import com.smartMall.websocket.MallAssistantWebSocketHandler;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Intelligent shopping assistant websocket configuration.
 */
@Configuration
@EnableWebSocket
public class MallAssistantWebSocketConfig implements WebSocketConfigurer {

    @Resource
    private MallAssistantWebSocketHandler mallAssistantWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(mallAssistantWebSocketHandler, "/ws/assistant")
                .setAllowedOrigins("*");
    }
}
