package dev.niels.pcpanel.device.light.control;

import dev.niels.pcpanel.helper.ByteArrayBuilder;

public interface IControlConfig {
  void appendToBuilder(ByteArrayBuilder builder);

  default ControlConfig toConfig() {
    return (ControlConfig) this;
  }

  // @formatter:off
  interface KnobControlConfig extends IControlConfig {}
  interface SliderLabelControlConfig extends IControlConfig {}
  interface SliderControlConfig extends IControlConfig {}
  interface LogoControlConfig extends IControlConfig {}
  interface BodyConfig extends IControlConfig {
    byte[][] toCommand();
    @Override default void appendToBuilder(ByteArrayBuilder builder) {
             builder.append(toCommand()[0]);
         }
  }
  // @formatter:on
}
