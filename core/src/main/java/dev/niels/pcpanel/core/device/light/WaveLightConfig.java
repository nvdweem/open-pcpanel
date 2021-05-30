package dev.niels.pcpanel.core.device.light;

import dev.niels.pcpanel.core.device.light.control.IControlConfig;
import dev.niels.pcpanel.core.helper.ByteArrayBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class WaveLightConfig extends LightConfig implements IControlConfig.BodyConfig {
  private int hue;
  private int brightness;
  private int speed;
  private boolean reverse;
  private boolean bounce;

  @Override
  public byte[][] toCommand() {
    return new byte[][]{new ByteArrayBuilder(5, 4, 3, hue, -1, brightness, speed, reverse ? 1 : 0, bounce ? 1 : 0).getBytes()};
  }
}
