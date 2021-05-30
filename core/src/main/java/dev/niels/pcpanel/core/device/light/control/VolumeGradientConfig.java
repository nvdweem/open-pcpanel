package dev.niels.pcpanel.core.device.light.control;

import dev.niels.pcpanel.core.JsonColor;
import dev.niels.pcpanel.core.helper.ByteArrayBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.awt.Color;

@Data
@EqualsAndHashCode(callSuper = true)
public class VolumeGradientConfig extends ControlConfig implements IControlConfig.SliderControlConfig {
  @JsonColor private Color color1;
  @JsonColor private Color color2;

  @Override
  public void doAppend(ByteArrayBuilder builder) {
    builder.append(3).append(color2).append(color1);
  }
}
