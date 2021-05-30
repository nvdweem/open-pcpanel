package dev.niels.pcpanel.device;

import dev.niels.pcpanel.device.light.LightConfig;
import dev.niels.pcpanel.device.light.StaticLightConfig;
import dev.niels.pcpanel.profile.Profile;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
  @Getter private List<Profile> profiles;
  @Getter private Profile activeProfile;

  public ConnectedDevice init(HidDevice device) {
    this.device = device;
    this.id = device.getSerialNumber();
    init(Device.getPanelType(device).orElseThrow(() -> new IllegalArgumentException("Init called for non-PCPanel device")));
    log.info("Device connected! {}", device);

    new Thread(this::run).start();
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
    setConfig(p.getLightConfig());
  }

  public ConnectedDevice setProfiles(List<Profile> profiles, boolean changeActive) {
    this.profiles = profiles;
    if (!profiles.isEmpty() && changeActive) {
      setActiveProfile(profiles.get(0));
    }
    return this;
  }

  public void setConfig(LightConfig config) {
    if (config == null) {
      config = new StaticLightConfig().setColor(Color.black);
    }
    sendCommand(config.toCommand());
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
      eventPublisher.publishEvent(new DeviceControlEvent(this, device, DeviceControlEvent.Type.knobRotate, knob, value));
    } else if (data[0] == 2) {
      var knob = data[1] & 0xFF;
      var value = data[2] & 0xFF;
      eventPublisher.publishEvent(new DeviceControlEvent(this, device, DeviceControlEvent.Type.knobPressed, knob, value));
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
