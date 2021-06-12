package pcpanel.dev.niels.pcpanel.natives;

import lombok.Data;

@Data
public class VolumeSession {
  private final long pid;
  private String process;
  private String icon;
  private int volume;
  private boolean muted;
}
