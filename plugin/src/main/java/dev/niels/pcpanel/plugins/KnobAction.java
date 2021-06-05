package dev.niels.pcpanel.plugins;

import dev.niels.pcpanel.plugins.config.ActionConfig;

public interface KnobAction<T extends ActionConfig> extends Action<T> {
  void buttonDown(Control control, T config);

  default void buttonUp(Control control, T config) {
  }
}
