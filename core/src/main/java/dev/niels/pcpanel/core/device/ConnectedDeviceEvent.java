package dev.niels.pcpanel.core.device;

import lombok.Data;

@Data
public class ConnectedDeviceEvent {
  private final ConnectedDevice device;

  private Integer controlIdx;
  private boolean knobAction;
}
