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
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppMuteControl implements KnobAction<AppMuteControl.AppMuteConfig> {
  private final VolumeControlService vcService;

  @Override public String getName() {
    return "Mute application";
  }

  @Override public Class<AppMuteConfig> getConfigurationClass() {
    return AppMuteConfig.class;
  }

  @Override public void buttonDown(Control control, AppMuteConfig config) {
    boolean muted = false;
    switch (Optional.ofNullable(config.getAction()).orElse("Toggle")) {
      case "Toggle":
        muted = vcService.toggleProcessMute(config.getApplication(), config.isOsd());
        break;
      case "Mute":
        vcService.setProcessMute(config.getApplication(), true, config.isOsd());
        muted = true;
        break;
      case "Unmute":
        vcService.setProcessMute(config.getApplication(), false, config.isOsd());
        break;
    }

    if (muted && config.isHasMuteColor()) {
      control.setSingleColor(config.getMuteColor());
    } else {
      control.setSingleColor(null);
    }
  }

  @Data
  public static class AppMuteConfig implements ActionConfig {
    @ConfigElement.Radio(label = "What to do", options = {"Toggle", "Mute", "Unmute"}, def = "Toggle") private String action;
    @ConfigElement.FilePicker(label = "Application") private String application;
    @ConfigElement.Checkbox(label = "Show OSD") private boolean osd;
    @ConfigElement.Checkbox(label = "Enable mute color") private boolean hasMuteColor;
    @ConfigElement.Color(label = "Mute color", def = "#F00") private Color muteColor;

    @Override public Class<AppMuteControl> getActionClass() {
      return AppMuteControl.class;
    }
  }
}
