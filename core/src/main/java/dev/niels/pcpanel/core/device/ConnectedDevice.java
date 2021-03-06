package dev.niels.pcpanel.core.device;

import dev.niels.pcpanel.core.device.light.LightConfig;
import dev.niels.pcpanel.core.device.light.StaticLightConfig;
import dev.niels.pcpanel.core.profile.Profile;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.hid4java.HidDevice;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Component
@RequiredArgsConstructor
public class ConnectedDevice {
  private static final int PACKET_LENGTH = 64;
  private final ApplicationEventPublisher eventPublisher;
  private HidDevice device;
  private boolean running = true;
  @Getter private final List<Integer> states = new ArrayList<>();
  @Getter private Device type;
  @Getter private String id;
  @Getter @Setter private List<Profile> profiles;
  @Getter private Profile activeProfile;
  @Getter private LightConfig currentLights;

  public ConnectedDevice init(HidDevice device) {
    this.device = device;
    this.id = device.getSerialNumber();
    init(Device.getPanelType(device).orElseThrow(() -> new IllegalArgumentException("Init called for non-PCPanel device")));
    log.info("Device connected! {}", device);
    return this;
  }

  public ConnectedDevice start() {
    new Thread(this::run, "Device|" + this.id).start();
    return this;
  }

  private void init(Device device) {
    this.type = device;
    for (int i = 0; i < device.getAnalogCount(); i++) {
      states.add(0);
    }
  }

  public void setActiveProfile(Profile p) {
    activeProfile = p;
    setCurrentLights(p.getLightConfig());
  }

  public void setCurrentLights(LightConfig currentLights) {
    this.currentLights = currentLights == null ? new StaticLightConfig().setColor(Color.black) : currentLights.copy();
    sendCommand(this.currentLights.toCommand());
  }

  public void sendCurrentConfig() {
    sendCommand(currentLights.toCommand());
  }

  @EventListener
  public void deviceDisconnected(DeviceEvent event) {
    if (!event.isConnected() && event.getDevice().equals(device)) {
      stop();
    }
  }

  @PreDestroy
  private void stop() {
    this.running = false;
  }

  private void run() {
    if (!device.open()) {
      log.error("Unable to open {}", device);
      return;
    }
    if (!profiles.isEmpty()) {
      setActiveProfile(profiles.get(0));
      eventPublisher.publishEvent(new ConnectedDeviceEvent(this));
    }

    device.setNonBlocking(false);
    sendCommand(new byte[]{1});

    try {
      while (running) {
        var read = doRead();
        if (read != null) {
          execute(read);
        }
      }
    } finally {
      device.close();
    }
  }

  private byte[] doRead() {
    var data = new byte[64];
    var val = this.device.read(data, 100);
    switch (val) {
      case -1:
        log.error("Unable to read from {}: {}", device, device.getLastErrorMessage());
        stop();
        break;
      case 0:
        break;
      default:
        return data;
    }
    return null;
  }

  private void execute(byte[] data) {
    if (data[0] == 1) {
      var knob = data[1] & 0xFF;
      var value = data[2] & 0xFF;
      states.set(knob, value);
      eventPublisher.publishEvent(new DeviceControlEvent(this, DeviceControlEvent.Type.knobRotate, knob, value));
    } else if (data[0] == 2) {
      var knob = data[1] & 0xFF;
      var value = data[2] & 0xFF;
      eventPublisher.publishEvent(new DeviceControlEvent(this, DeviceControlEvent.Type.knobPressed, knob, value));
    } else {
      log.error("Unknown input: {}", Arrays.toString(data));
    }
  }

  private void sendCommand(byte[][] infos) {
    StreamEx.of(infos).forEach(this::sendCommand);
  }

  private void sendCommand(byte[] info) {
    if (info.length > PACKET_LENGTH) {
      throw new IllegalArgumentException("info cannot be greater than packet_length");
    }

    int val = this.device.write(Arrays.copyOf(info, PACKET_LENGTH), PACKET_LENGTH, (byte) 0);
    if (val >= 0) {
      log.trace("> [{}]", val);
    } else {
      log.error("Unable to write to {}: {}; {}; {}", this.device, this.device.getLastErrorMessage(), val, Arrays.toString(info));
    }
  }
}
