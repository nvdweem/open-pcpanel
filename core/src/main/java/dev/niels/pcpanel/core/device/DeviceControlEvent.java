package dev.niels.pcpanel.core.device;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.hid4java.HidDevice;

@Getter
@ToString
@RequiredArgsConstructor
public class DeviceControlEvent {
  public enum Type {
    knobRotate, knobPressed
  }

  private final ConnectedDevice connectedDevice;
  private final HidDevice device;
  private final Type type;
  private final int key;
  private final int value;
}
