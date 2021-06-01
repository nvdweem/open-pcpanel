package pcpanel.dev.niels.pcpanel;

import dev.niels.pcpanel.plugins.Action;
import dev.niels.pcpanel.plugins.config.ActionConfig;
import dev.niels.pcpanel.plugins.config.ConfigElement;

public class FocusVolumeConfig implements ActionConfig {
  @ConfigElement.Label("Nothing to configure :)") private String dummy;

  @Override public Class<? extends Action<?>> getActionClass() {
    return FocusVolumeControl.class;
  }
}
