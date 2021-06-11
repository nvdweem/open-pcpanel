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
public class GradientConfig extends ControlConfig implements IControlConfig.SliderControlConfig, IControlConfig.KnobControlConfig {
  @JsonColor private Color color1;
  @JsonColor private Color color2;

  @Override
  public void doAppend(ByteArrayBuilder builder) {
    builder.append(2).append(color2).append(color1);
  }

  @Override public GradientConfig copy() {
    return toBuilder().build();
  }
}
