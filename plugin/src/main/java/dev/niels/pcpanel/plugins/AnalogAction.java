package dev.niels.pcpanel.plugins;

import dev.niels.pcpanel.plugins.config.ActionConfig;

public interface AnalogAction<T extends ActionConfig> extends Action<T> {
  void triggerAction(Control control, T config, int sliderPos);
}
