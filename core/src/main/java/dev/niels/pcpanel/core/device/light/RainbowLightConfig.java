package dev.niels.pcpanel.core.device.light;

import dev.niels.pcpanel.core.device.light.control.IControlConfig;
import dev.niels.pcpanel.core.helper.ByteArrayBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
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

  @Override public RainbowLightConfig copy() {
    return toBuilder().build();
  }
}
