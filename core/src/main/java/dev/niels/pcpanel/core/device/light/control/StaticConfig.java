package dev.niels.pcpanel.core.device.light.control;

import dev.niels.pcpanel.core.JsonColor;
import dev.niels.pcpanel.core.helper.ByteArrayBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.awt.Color;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
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

  @Override public StaticConfig copy() {
    return toBuilder().build();
  }
}
