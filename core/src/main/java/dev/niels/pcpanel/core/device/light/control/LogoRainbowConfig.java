package dev.niels.pcpanel.core.device.light.control;

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
public class LogoRainbowConfig extends ControlConfig implements IControlConfig.LogoControlConfig {
  private int brightness;
  private int speed;

  @Override
  public void doAppend(ByteArrayBuilder builder) {
    builder.append(2, -1, brightness, speed);
  }

  @Override public LogoRainbowConfig copy() {
    return toBuilder().build();
  }
}
