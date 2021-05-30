package dev.niels.pcpanel.device.light;

import dev.niels.pcpanel.JsonColor;
import dev.niels.pcpanel.device.light.control.IControlConfig;
import dev.niels.pcpanel.helper.ByteArrayBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.awt.Color;

@Data
@EqualsAndHashCode(callSuper = true)
public class StaticLightConfig extends LightConfig implements IControlConfig.BodyConfig {
  @JsonColor private Color color;

  @Override
  public byte[][] toCommand() {
    return new byte[][]{new ByteArrayBuilder(5, 4, 2).append(color).getBytes()};
  }
}
