package dev.niels.pcpanel.plugins;

import dev.niels.pcpanel.plugins.config.ConfigPageBuilder;

import java.util.List;

public interface AnalogAction {
  String getName();

  List<ConfigPageBuilder.ConfigElement> getConfigElements();

  default String getImpl() {
    return this.getClass().getName();
  }
}
