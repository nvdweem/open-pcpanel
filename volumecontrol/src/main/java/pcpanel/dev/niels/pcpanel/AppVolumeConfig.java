package pcpanel.dev.niels.pcpanel;

import dev.niels.pcpanel.plugins.config.ActionConfig;
import dev.niels.pcpanel.plugins.config.ConfigElement;
import lombok.Data;

@Data
public class AppVolumeConfig implements ActionConfig {
  @ConfigElement.FilePicker(label = "Application", extension = "exe") private String application;

  @ConfigElement.Checkbox(label = "Show OSD") private boolean osd = false;
  @ConfigElement.Checkbox(label = "Logarithmic scaling") private boolean logScaling = false;
  @ConfigElement.Number(label = "Trim min") private Integer trimMin;
  @ConfigElement.Number(label = "Trim max") private Integer trimMax;

  @Override public Class<AppVolumeControl> getActionClass() {
    return AppVolumeControl.class;
  }
}
