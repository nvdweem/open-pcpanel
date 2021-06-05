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
}
