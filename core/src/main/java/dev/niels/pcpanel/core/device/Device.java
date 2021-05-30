package dev.niels.pcpanel.core.device;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hid4java.HidDevice;

import java.util.Optional;

@RequiredArgsConstructor
@Getter
public enum Device {
  PCPANEL_RGB(1240, 60242, 4, 4),
  PCPANEL_MINI(1155, 41924, 4, 4),
  PCPANEL_PRO(1155, 41925, 9, 5);

  private final int vid;
  private final int pid;
  private final int analogCount;
  private final int buttonCount;

  public static Optional<Device> getPanelType(HidDevice device) {
    return getPanelType(device.getVendorId(), device.getProductId());
  }

  public static Optional<Device> getPanelType(int vid, int pid) {
    for (var device : Device.values()) {
      if (device.getVid() == vid && device.getPid() == pid) {
        return Optional.of(device);
      }
    }
    return Optional.empty();
  }
}
