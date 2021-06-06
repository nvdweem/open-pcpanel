package dev.niels.pcpanel.core.device;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class DeviceControlLightChangedEvent {
  private final ConnectedDevice connectedDevice;
  private final String type;
  private final int key;
}
