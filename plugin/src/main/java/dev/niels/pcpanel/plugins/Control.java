package dev.niels.pcpanel.plugins;

import java.awt.Color;

public interface Control {
  boolean isSlider();

  void setSingleColor(Color c);

  void setMultiColor(Color bottom, Color top);
}
