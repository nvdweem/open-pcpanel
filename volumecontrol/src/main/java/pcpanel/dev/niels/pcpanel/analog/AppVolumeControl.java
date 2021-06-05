package pcpanel.dev.niels.pcpanel.analog;

import dev.niels.pcpanel.plugins.AnalogAction;
import dev.niels.pcpanel.plugins.Control;
import dev.niels.pcpanel.plugins.config.ActionConfig;
import dev.niels.pcpanel.plugins.config.ConfigElement;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Component
@RestController
public class AppVolumeControl implements AnalogAction<AppVolumeControl.AppVolumeConfig> {
  @Override public String getName() {
    return "App volume";
  }

  @Override public Class<AppVolumeConfig> getConfigurationClass() {
    return AppVolumeConfig.class;
  }

  @Override public void triggerAction(Control control, AppVolumeConfig config, int sliderPos) {
    log.error("Action not implemented yet");
  }

  @Data
  public static class AppVolumeConfig implements ActionConfig {
    @ConfigElement.FilePicker(label = "Application", extension = "exe") private String application;

    @ConfigElement.Checkbox(label = "Show OSD") private boolean osd = false;
    @ConfigElement.Checkbox(label = "Logarithmic scaling", def = true) private boolean logScaling = false;
    @ConfigElement.Number(label = "Trim min", def = "0") private Integer trimMin;
    @ConfigElement.Number(label = "Trim max", def = "100") private Integer trimMax;

    @Override public Class<AppVolumeControl> getActionClass() {
      return AppVolumeControl.class;
    }
  }

}
