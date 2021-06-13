package pcpanel.dev.niels.pcpanel.analog;

import dev.niels.pcpanel.plugins.AnalogAction;
import dev.niels.pcpanel.plugins.Control;
import dev.niels.pcpanel.plugins.config.ActionConfig;
import dev.niels.pcpanel.plugins.config.ConfigElement;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import pcpanel.dev.niels.pcpanel.Util;
import pcpanel.dev.niels.pcpanel.natives.VolumeControlService;

@Slf4j
@Component
@RestController
@RequiredArgsConstructor
public class DeviceVolumeControl implements AnalogAction<DeviceVolumeControl.DeviceVolumeConfig> {
  private final VolumeControlService vcService;

  @Override public String getName() {
    return "Device volume";
  }

  @Override public Class<DeviceVolumeConfig> getConfigurationClass() {
    return DeviceVolumeConfig.class;
  }

  @Override public void triggerAction(Control control, DeviceVolumeConfig config, int sliderPos) {
    var device = Util.determineDevice(config.getWhich(), config.getDevice(), config.getWhichDefault());
    if (!StringUtils.hasText(device)) {
      log.warn("No device found for {}, doing nothing instead.", config);
      return;
    }

    var pos = Util.translateAnalog(sliderPos, config.isLogScaling(), config.getTrimMin(), config.getTrimMax());
    vcService.setDeviceVolume(device, pos, config.isOsd());
  }

  @Data
  public static class DeviceVolumeConfig implements ActionConfig {
    @ConfigElement.Radio(label = "What to mute", options = {"Default", "Specific"}, def = "Default") private String which;
    @ConfigElement.Radio(label = "If default, which default", options = {"Multimedia out", "Multimedia in", "Communications out", "Communications in"}, def = "Multimedia out") private String whichDefault;
    @ConfigElement.Dropdown(label = "Device", listSource = "volumecontrol/devices") private String device;

    @ConfigElement.Checkbox(label = "Show OSD") private boolean osd = false;
    @ConfigElement.Checkbox(label = "Logarithmic scaling", def = true) private boolean logScaling = false;
    @ConfigElement.Number(label = "Trim min", def = "0") private Integer trimMin;
    @ConfigElement.Number(label = "Trim max", def = "100") private Integer trimMax;

    @Override public Class<DeviceVolumeControl> getActionClass() {
      return DeviceVolumeControl.class;
    }
  }
}
