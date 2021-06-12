package pcpanel.dev.niels.pcpanel.natives;

import lombok.Data;

@Data
public class VolumeDevice {
  private final String id;
  private String name;
  private int volume;
  private boolean muted;
}
