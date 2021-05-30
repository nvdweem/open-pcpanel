package dev.niels.pcpanel.core.device.light.control;

import dev.niels.pcpanel.core.JsonColor;
import dev.niels.pcpanel.core.helper.ByteArrayBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.awt.Color;

@Data
@EqualsAndHashCode(callSuper = true)
public class StaticConfig extends ControlConfig implements IControlConfig.KnobControlConfig, IControlConfig.SliderControlConfig, IControlConfig.SliderLabelControlConfig, IControlConfig.LogoControlConfig {
  @JsonColor private Color color1;
  @JsonColor private Color color2;

  public StaticConfig setColors(Color color) {
    color1 = color;
    color2 = color;
    return this;
  }

  @Override
  public void doAppend(ByteArrayBuilder builder) {
    builder.append(1).append(color1).append(color2);
  }
}
