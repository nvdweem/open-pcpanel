package dev.niels.pcpanel.core.plugin;

import dev.niels.pcpanel.core.device.ControlWrapper;
import dev.niels.pcpanel.core.profile.Profile;
import dev.niels.pcpanel.plugins.Control;
import dev.niels.pcpanel.plugins.KnobAction;
import dev.niels.pcpanel.plugins.config.ActionConfig;
import dev.niels.pcpanel.plugins.config.ConfigElement;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class SwitchProfileControl implements KnobAction<SwitchProfileControl.SwitchProfileConfig> {

  @Override public String getName() {
    return "Switch profile";
  }

  @Override public Class<SwitchProfileConfig> getConfigurationClass() {
    return SwitchProfileConfig.class;
  }

  @Override public void buttonDown(Control control, SwitchProfileConfig config) {
    if (!StringUtils.hasText(config.getProfile())) {
      log.warn("No profile selected, nothing so switch to");
      return;
    }

    var toSelectId = Long.parseLong(config.getProfile());
    var device = ((ControlWrapper) control).getDevice();
    var profile = StreamEx.of(device.getProfiles()).filterBy(Profile::getId, toSelectId).findFirst();
    if (profile.isPresent()) {
      device.setActiveProfile(profile.get());
    } else {
      log.warn("Unable to switch to profile {}, profile not found", toSelectId);
    }
  }

  @Data
  public static class SwitchProfileConfig implements ActionConfig {
    @ConfigElement.Dropdown(label = "Profile", listSource = "core/profiles") private String profile;

    @Override public Class<? extends SwitchProfileControl> getActionClass() {
      return SwitchProfileControl.class;
    }
  }
}
