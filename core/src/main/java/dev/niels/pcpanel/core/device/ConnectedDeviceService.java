package dev.niels.pcpanel.core.device;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.niels.pcpanel.core.profile.Actions;
import dev.niels.pcpanel.core.profile.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hid4java.HidDevice;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Order(0)
@Service
@RequiredArgsConstructor
public class ConnectedDeviceService {
  private final ObjectFactory<ConnectedDevice> connectedDeviceFactory;
  private final Map<String, ConnectedDevice> devices = new HashMap<>();
  private final ObjectMapper objectMapper;
  private final ProfileRepository profileRepository;

  @EventListener
  public void deviceConnected(DeviceEvent event) {
    var deviceId = event.getDevice().getSerialNumber();
    if (event.isConnected()) {
      deviceAdded(deviceId, event.getDevice());
    } else {
      devices.remove(deviceId);
    }
  }

  private void deviceAdded(String deviceId, HidDevice device) {
    devices.put(deviceId, updateProfiles(connectedDeviceFactory.getObject().init(device)).start());
  }

  public ConnectedDevice updateProfiles(ConnectedDevice device) {
    var profiles = profileRepository.findByDevice(device.getId());
    profiles.forEach(p -> {
      p.init(objectMapper);
      if (p.getActionsConfig() == null) {
        p.setActionsConfig(Actions.forDevice(device.getType()));
      }
    });
    device.setProfiles(profiles);
    return device;
  }

  public Collection<ConnectedDevice> getDevices() {
    return new ArrayList<>(devices.values());
  }

  public Optional<ConnectedDevice> getDevice(String id) {
    return Optional.ofNullable(devices.get(id));
  }
}
