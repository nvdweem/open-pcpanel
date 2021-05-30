package dev.niels.pcpanel.core.device.light;

import dev.niels.pcpanel.core.device.light.control.IControlConfig;
import dev.niels.pcpanel.core.helper.ByteArrayBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RainbowLightConfig extends LightConfig implements IControlConfig.BodyConfig {
  private int phaseShift;
  private int brightness;
  private int speed;
  private boolean reverse;

  @Override
  public byte[][] toCommand() {
    return new byte[][]{new ByteArrayBuilder(5, 4, 1, phaseShift, -1, brightness, speed, reverse ? 1 : 0).getBytes()};
  }
}
