package dev.niels.pcpanel.core.device.light;

import dev.niels.pcpanel.core.JsonColor;
import dev.niels.pcpanel.core.device.light.control.IControlConfig;
import dev.niels.pcpanel.core.helper.ByteArrayBuilder;
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
