package dev.niels.pcpanel.device.light.control;

import dev.niels.pcpanel.helper.ByteArrayBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.awt.Color;

@Data
@EqualsAndHashCode(callSuper = true)
public class EmptyConfig extends StaticConfig implements IControlConfig, IControlConfig.KnobControlConfig, IControlConfig.SliderControlConfig, IControlConfig.SliderLabelControlConfig, IControlConfig.LogoControlConfig {
  public EmptyConfig() {
    setColors(Color.black);
  }

  @Override
  public void doAppend(ByteArrayBuilder builder) {
  }
}
