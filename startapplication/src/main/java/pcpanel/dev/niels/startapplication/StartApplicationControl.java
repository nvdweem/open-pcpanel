package pcpanel.dev.niels.startapplication;

import dev.niels.pcpanel.plugins.Action;
import dev.niels.pcpanel.plugins.Control;
import dev.niels.pcpanel.plugins.KnobAction;
import dev.niels.pcpanel.plugins.config.ActionConfig;
import dev.niels.pcpanel.plugins.config.ConfigElement;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

@Slf4j
@Component
public class StartApplicationControl implements KnobAction<StartApplicationControl.StartApplicationConfig> {

  @Override public String getName() {
    return "Start application";
  }

  @Override public Class<StartApplicationConfig> getConfigurationClass() {
    return StartApplicationConfig.class;
  }

  @Override public void buttonDown(Control control, StartApplicationConfig config) {
    var file = new File(config.getApplication());
    if (file.exists()) {
      try {
        Desktop.getDesktop().open(file);
      } catch (IOException e) {
        log.error("Unable to open {}", file);
      }
    }
  }

  @Data
  public static class StartApplicationConfig implements ActionConfig {
    @ConfigElement.FilePicker(label = "Application") private String application;

    @Override public Class<? extends Action<?>> getActionClass() {
      return StartApplicationControl.class;
    }
  }
}
