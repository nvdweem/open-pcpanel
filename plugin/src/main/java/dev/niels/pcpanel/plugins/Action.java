package dev.niels.pcpanel.plugins;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface Action<T> {
  String getName();

  @JsonIgnore
  Class<T> getConfigurationClass();

  default String getImpl() {
    return this.getClass().getName();
  }
}
