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
import pcpanel.dev.niels.pcpanel.natives.VolumeDevice;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class SetDefaultDeviceControl implements KnobAction<SetDefaultDeviceControl.SetDefaultDeviceConfig> {
  private final VolumeControlService vcService;

  @Override public String getName() {
    return "Set default device";
  }

  @Override public Class<SetDefaultDeviceConfig> getConfigurationClass() {
    return SetDefaultDeviceConfig.class;
  }

  @Override public void buttonDown(Control control, SetDefaultDeviceConfig config) {
    var roles = Optional.ofNullable(config.whichDefault).filter(v -> !v.startsWith("Multimedia")).map(v -> Set.of(VolumeDevice.DeviceRole.communications)).orElse(Set.of(VolumeDevice.DeviceRole.multimedia, VolumeDevice.DeviceRole.console));
    roles.forEach(role -> vcService.setActiveDevice(config.getDevice(), role, false));
  }

  @Data
  public static class SetDefaultDeviceConfig implements ActionConfig {
    @ConfigElement.Radio(label = "Default for", options = {"Multimedia", "Communications"}, def = "Multimedia") private String whichDefault;
    @ConfigElement.Dropdown(label = "Device", listSource = "volumecontrol/devices") private String device;

    @Override public Class<SetDefaultDeviceControl> getActionClass() {
      return SetDefaultDeviceControl.class;
    }
  }
}
