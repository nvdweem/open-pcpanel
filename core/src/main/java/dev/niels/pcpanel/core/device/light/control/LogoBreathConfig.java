package dev.niels.pcpanel.core.device.light.control;

import dev.niels.pcpanel.core.helper.ByteArrayBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LogoBreathConfig extends ControlConfig implements IControlConfig.LogoControlConfig {
  private int hue;
  private int brightness;
  private int speed;

  @Override
  public void doAppend(ByteArrayBuilder builder) {
    builder.append(3, hue, -1, brightness, speed);
  }
}
