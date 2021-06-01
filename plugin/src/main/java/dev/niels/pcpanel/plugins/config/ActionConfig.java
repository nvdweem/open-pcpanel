package dev.niels.pcpanel.plugins.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dev.niels.pcpanel.plugins.Action;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "_type")
public interface ActionConfig {
  Class<? extends Action<?>> getActionClass();
}
