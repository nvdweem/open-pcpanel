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
public class BreathLightConfig extends LightConfig implements IControlConfig.BodyConfig {
  private int hue;
  private int brightness;
  private int speed;

  @Override
  public byte[][] toCommand() {
    return new byte[][]{new ByteArrayBuilder(5, 4, 4, hue, -1, brightness, speed).getBytes()};
  }

  @Override public BreathLightConfig copy() {
    return toBuilder().build();
  }
}
