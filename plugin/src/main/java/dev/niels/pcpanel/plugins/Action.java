package dev.niels.pcpanel.plugins;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.niels.pcpanel.plugins.config.ActionConfig;

public interface Action<T extends ActionConfig> {
  String getName();

  @JsonIgnore
  Class<T> getConfigurationClass();

  default String getImpl() {
    return this.getClass().getName();
  }

  /**
   * Allows initialization of the action. This can be used to give the control an initial state-determined color
   *
   * @param control The control for which the action is initialized
   * @param cfg     The configuration for the action
   */
  default void init(Control control, T cfg) {
  }
}
