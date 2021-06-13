package pcpanel.dev.niels.pcpanel.natives;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class VolumeDevice {
  public enum DeviceType {
    render, capture, all;

    static DeviceType fromInt(int idx) {
      return values()[idx];
    }
  }

  public enum DeviceRole {
    console, multimedia, communications;

    static DeviceRole fromInt(int idx) {
      return values()[idx];
    }
  }

  private final String id;
  private String name;
  private int volume;
  private boolean muted;
  private DeviceType type;
  private Set<DeviceRole> defaultFor = new HashSet<>();
}
