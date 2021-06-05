package pcpanel.dev.niels.pcpanel.knob;

import dev.niels.pcpanel.plugins.Control;
import dev.niels.pcpanel.plugins.KnobAction;
import dev.niels.pcpanel.plugins.config.ActionConfig;
import dev.niels.pcpanel.plugins.config.ConfigElement;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pcpanel.dev.niels.pcpanel.natives.VolumeControlService;

import java.awt.Color;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceMuteControl implements KnobAction<DeviceMuteControl.DeviceMuteConfig> {
  private final VolumeControlService vcService;

  @Override public String getName() {
    return "Mute device";
  }

  @Override public Class<DeviceMuteConfig> getConfigurationClass() {
    return DeviceMuteConfig.class;
  }

  @Override public void triggerAction(Control control, DeviceMuteConfig config, boolean down) {
    if (down) {
      if (vcService.toggleDeviceMute(config.getDevice(), true)) {
        if (config.isHasMuteColor()) {
          control.setSingleColor(config.getMuteColor());
        }
      } else {
        control.setSingleColor(null);
      }
    }
  }

  @Data
  public static class DeviceMuteConfig implements ActionConfig {
    @ConfigElement.Dropdown(label = "Device", listSource = "volumecontrol/devices") private String device;
    @ConfigElement.Checkbox(label = "Enable mute color") private boolean hasMuteColor;
    @ConfigElement.Color(label = "Mute color", def = "#F00") private Color muteColor;

    @Override public Class<DeviceMuteControl> getActionClass() {
      return DeviceMuteControl.class;
    }
  }
}
