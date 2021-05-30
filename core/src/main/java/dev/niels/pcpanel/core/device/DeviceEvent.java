package dev.niels.pcpanel.core.device;

import lombok.Data;
import org.hid4java.HidDevice;

@Data
public class DeviceEvent {
  private final boolean connected;
  private final HidDevice device;
}
