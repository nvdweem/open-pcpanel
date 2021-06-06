package dev.niels.pcpanel.core.web.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.niels.pcpanel.core.device.ConnectedDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
  @Autowired private ObjectMapper mapper;
  @Autowired private ConnectedDeviceService connectedDeviceService;

  @Override public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(deviceWebSocketHandler(), "/api/deviceSocket");
  }

  @Bean
  public DeviceWebsocketHandler deviceWebSocketHandler() {
    return new DeviceWebsocketHandler(connectedDeviceService, mapper);
  }
}
