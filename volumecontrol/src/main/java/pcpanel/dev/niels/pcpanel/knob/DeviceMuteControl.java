package pcpanel.dev.niels.pcpanel.knob;

import dev.niels.pcpanel.plugins.Control;
import dev.niels.pcpanel.plugins.KnobAction;
import dev.niels.pcpanel.plugins.config.ActionConfig;
import dev.niels.pcpanel.plugins.config.ConfigElement;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import java.awt.Color;

@Slf4j
@Component
@RestController
public class DeviceMuteControl implements KnobAction<DeviceMuteControl.DeviceMuteConfig> {
  @Override public String getName() {
    return "Mute device";
  }

  @Override public Class<DeviceMuteConfig> getConfigurationClass() {
    return DeviceMuteConfig.class;
  }

  @Override public void triggerAction(Control control, DeviceMuteConfig config, boolean down) {
    if (down) {
      control.setSingleColor(Color.red);
    } else {
      control.setSingleColor(Color.green);
    }
  }

  @Data
  public static class DeviceMuteConfig implements ActionConfig {
    @ConfigElement.Text(label = "Device") private String device;

    @Override public Class<DeviceMuteControl> getActionClass() {
      return DeviceMuteControl.class;
    }
  }
}
