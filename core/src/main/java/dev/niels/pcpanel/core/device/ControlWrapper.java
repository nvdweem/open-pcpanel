package dev.niels.pcpanel.core.device;

import dev.niels.pcpanel.plugins.Control;
import lombok.Data;

import java.awt.Color;

@Data
public class ControlWrapper implements Control {
  private final boolean slider;
  private final SingleColorSetter scs;
  private final TwoColorSetter tcs;
  private final ConnectedDevice device;

  @Override public void setSingleColor(Color c) {
    scs.setColor(c);
  }

  @Override public void setMultiColor(Color bottom, Color top) {
    tcs.setColor(bottom, top);
  }

  interface SingleColorSetter {
    void setColor(Color c);
  }

  interface TwoColorSetter {
    void setColor(Color c1, Color c2);
  }
}
