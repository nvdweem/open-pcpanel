package dev.niels.pcpanel.plugins.config;

import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ConfigPageBuilder {
  @Getter private final List<ConfigElement> configElements = new ArrayList<>();

  public ConfigPageBuilder addElement(String name, String label, ConfigElementType type) {
    configElements.add(new ConfigElement(name, label, type));
    return this;
  }

  @Data
  public static class ConfigElement {
    private final String name;
    private final String label;
    private final ConfigElementType type;
  }
}
