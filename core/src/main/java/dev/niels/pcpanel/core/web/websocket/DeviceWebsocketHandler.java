package dev.niels.pcpanel.core.web.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.niels.pcpanel.core.JsonColor;
import dev.niels.pcpanel.core.device.ConnectedDevice;
import dev.niels.pcpanel.core.device.ConnectedDeviceService;
import dev.niels.pcpanel.core.device.DeviceControlEvent;
import dev.niels.pcpanel.core.device.DeviceControlLightChangedEvent;
import dev.niels.pcpanel.core.device.light.CustomLightConfig;
import dev.niels.pcpanel.core.device.light.StaticLightConfig;
import dev.niels.pcpanel.core.device.light.control.ControlConfig;
import dev.niels.pcpanel.core.device.light.control.GradientConfig;
import dev.niels.pcpanel.core.device.light.control.StaticConfig;
import dev.niels.pcpanel.core.device.light.control.VolumeGradientConfig;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class DeviceWebsocketHandler extends TextWebSocketHandler {
  private final ConnectedDeviceService deviceService;
  private final ObjectMapper mapper;
  private final Map<String, Set<WebSocketSession>> sessions = new HashMap<>();
  private final Map<WebSocketSession, String> sessionSubscription = new HashMap<>();

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    removeSession(session);
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    addSession(session, message.getPayload());
    sendInitial(session, message.getPayload());
  }

  private void sendInitial(WebSocketSession session, String deviceId) {
    var device = deviceService.getDevice(deviceId).orElseThrow(() -> new IllegalArgumentException("Unknown device"));

    try {
      for (int i = 0; i < device.getType().getAnalogCount(); i++) {
        session.sendMessage(new TextMessage(mapper.writeValueAsString(getControlEvent(device, i, device.getStates().get(i)))));
      }

      if (device.getActiveProfile().getLightConfig() instanceof CustomLightConfig) {
        var clc = (CustomLightConfig) device.getActiveProfile().getLightConfig();
        for (int i = 0; i < device.getType().getAnalogCount() - device.getType().getButtonCount(); i++) {
          var sliderConfig = clc.getSliderLabels().get(i);
          var msg = mapper.writeValueAsString(new ControlEvent("slider-label", i, 0).setColor(sliderConfig instanceof StaticConfig ? ((StaticConfig) sliderConfig).getColor1() : null));
          session.sendMessage(new TextMessage(msg));
        }

        if (clc.getLogo() instanceof StaticConfig) {
          var msg = mapper.writeValueAsString(new ControlEvent("logo", 0, 0).setColor(((StaticConfig) clc.getLogo()).getColor1()));
          session.sendMessage(new TextMessage(msg));
        }
      }
    } catch (Exception e) {
      log.warn("Unable to send initial state");
    }
  }

  @EventListener
  public void deviceEvent(DeviceControlEvent event) {
    if (event.getType() != DeviceControlEvent.Type.knobRotate) {
      return;
    }
    sendToAll(event.getConnectedDevice().getId(), getControlEvent(event.getConnectedDevice(), event.getKey(), event.getValue()));
  }

  @EventListener
  public void lightChangedEvent(DeviceControlLightChangedEvent event) {
    if (!StringUtils.hasText(event.getType())) {
      sendToAll(event.getConnectedDevice().getId(), getControlEvent(event.getConnectedDevice(), event.getKey(), event.getConnectedDevice().getStates().get(event.getKey())));
    }
  }

  private void sendToAll(String deviceId, Object data) {
    try {
      var dataStr = mapper.writeValueAsString(data);
      var ss = sessions.computeIfAbsent(deviceId, x -> new HashSet<>());
      var itt = ss.iterator();
      while (itt.hasNext()) {
        var s = itt.next();
        try {
          s.sendMessage(new TextMessage(dataStr));
        } catch (Exception e) {
          log.error("Unable to stream device control event");
          itt.remove();
        }
      }
    } catch (JsonProcessingException e) {
      log.error("Unable to stream device control event");
    }
  }

  private ControlEvent getControlEvent(ConnectedDevice device, int key, int value) {
    return determineColors(device, key, value, new ControlEvent(null, key, value));
  }

  private ControlEvent determineColors(ConnectedDevice device, int key, int value, ControlEvent target) {
    var lc = device.getActiveProfile().getLightConfig();
    if (lc instanceof StaticLightConfig) {
      return target.setColor(((StaticLightConfig) lc).getColor());
    }
    if (!(lc instanceof CustomLightConfig)) {
      // We don't do colors for non-custom light configs (yet?)
      return target;
    }

    var clc = (CustomLightConfig) lc;
    ControlConfig config;
    if (key < device.getType().getButtonCount()) {
      config = clc.getKnobs().get(key);

      if (config instanceof StaticConfig) {
        target.setColor(((StaticConfig) config).getColor1());
      } else if (config instanceof GradientConfig) {
        target.setColor(interpolate(((GradientConfig) config).getColor2(), ((GradientConfig) config).getColor1(), value / 255f));
      }
    } else {
      config = clc.getSliders().get(key - device.getType().getButtonCount());

      if (config instanceof StaticConfig) {
        var color = ((StaticConfig) config).getColor1();
        target.setColor(color).setColor2(color).setColor3(color).setColor4(color).setColor5(color);
      } else if (config instanceof VolumeGradientConfig) {
        var color1 = ((VolumeGradientConfig) config).getColor1();
        var color2 = ((VolumeGradientConfig) config).getColor2();
        target
          .setColor(interpolate(color1, color2, 0))
          .setColor2(interpolate(color1, color2, .25))
          .setColor3(interpolate(color1, color2, .50))
          .setColor4(interpolate(color1, color2, .75))
          .setColor5(interpolate(color1, color2, 1));
      }
    }

    return target;
  }

  private Color interpolate(Color from, Color to, double t) {
    if (t <= 0.0) {
      return from;
    }
    if (t >= 1.0) {
      return to;
    }
    var fromRgb = from.getRGBColorComponents(new float[]{0, 0, 0});
    var toRgb = to.getRGBColorComponents(new float[]{0, 0, 0});
    float ft = (float) t;
    return new Color(
      fromRgb[0] + (toRgb[0] - fromRgb[0]) * ft,
      fromRgb[1] + (toRgb[1] - fromRgb[1]) * ft,
      fromRgb[2] + (toRgb[2] - fromRgb[2]) * ft
    );
  }


  private void addSession(WebSocketSession session, String subscription) {
    removeSession(session);
    sessionSubscription.put(session, subscription);
    sessions.computeIfAbsent(subscription, x -> new HashSet<>()).add(session);
  }

  private void removeSession(WebSocketSession session) {
    var sub = sessionSubscription.remove(session);
    var set = sessions.get(sub);
    if (set != null) {
      set.remove(session);
    }
  }

  @Data
  public static class ControlEvent {
    private final String type;
    private final int idx;
    private final int value;
    @JsonColor private Color color;
    @JsonColor private Color color2;
    @JsonColor private Color color3;
    @JsonColor private Color color4;
    @JsonColor private Color color5;
  }
}
