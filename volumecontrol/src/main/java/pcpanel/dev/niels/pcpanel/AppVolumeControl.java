package pcpanel.dev.niels.pcpanel;

import dev.niels.pcpanel.plugins.AnalogAction;
import dev.niels.pcpanel.plugins.Control;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Component
@RestController
public class AppVolumeControl implements AnalogAction<AppVolumeConfig> {
  @Override public String getName() {
    return "App volume";
  }

  @Override public Class<AppVolumeConfig> getConfigurationClass() {
    return AppVolumeConfig.class;
  }

  @Override public void triggerAction(Control control, AppVolumeConfig config, int sliderPos) {
    log.error("Action not implemented yet");
  }
}
