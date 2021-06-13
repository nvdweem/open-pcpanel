package pcpanel.dev.niels.pcpanel.knob;

import dev.niels.pcpanel.plugins.Control;
import dev.niels.pcpanel.plugins.KnobAction;
import dev.niels.pcpanel.plugins.config.ActionConfig;
import dev.niels.pcpanel.plugins.config.ConfigElement;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import pcpanel.dev.niels.pcpanel.Util;
import pcpanel.dev.niels.pcpanel.natives.VolumeControlService;

import java.awt.Color;
import java.util.Optional;

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

  @Override public void buttonDown(Control control, DeviceMuteConfig config) {
    var device = Util.determineDevice(config.getWhich(), config.getDevice(), config.getWhichDefault());
    if (!StringUtils.hasText(device)) {
      log.warn("No device found for {}, doing nothing instead.", config);
      return;
    }
    var osd = config.isOsd();

    boolean muted = false;
    switch (Optional.ofNullable(config.getAction()).orElse("Toggle")) {
      case "Toggle":
        muted = vcService.toggleDeviceMute(device, osd);
        break;
      case "Mute":
        vcService.setDeviceMute(device, true, osd);
        muted = true;
        break;
      case "Unmute":
        vcService.setDeviceMute(device, false, osd);
        break;
    }

    if (muted && config.isHasMuteColor()) {
      control.setSingleColor(config.getMuteColor());
    } else {
      control.setSingleColor(null);
    }
  }

  @Data
  public static class DeviceMuteConfig implements ActionConfig {
    @ConfigElement.Radio(label = "What to do", options = {"Toggle", "Mute", "Unmute"}, def = "Toggle") private String action;
    @ConfigElement.Radio(label = "What to mute", options = {"Default", "Specific"}, def = "Default") private String which;
    @ConfigElement.Radio(label = "If default, which default", options = {"Multimedia out", "Multimedia in", "Communications out", "Communications in"}, def = "Multimedia out") private String whichDefault;
    @ConfigElement.Dropdown(label = "Device", listSource = "volumecontrol/devices") private String device;
    @ConfigElement.Checkbox(label = "Show OSD") private boolean osd;
    @ConfigElement.Checkbox(label = "Enable mute color") private boolean hasMuteColor;
    @ConfigElement.Color(label = "Mute color", def = "#F00") private Color muteColor;

    @Override public Class<DeviceMuteControl> getActionClass() {
      return DeviceMuteControl.class;
    }
  }
}
