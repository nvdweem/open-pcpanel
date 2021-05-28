package dev.niels.pcpanel.device.light.control;

import dev.niels.pcpanel.helper.ByteArrayBuilder;

public interface IControlConfig {
    void appendToBuilder(ByteArrayBuilder builder);

    // @formatter:off
    interface KnobControlConfig extends IControlConfig {}
    interface SliderLabelControlConfig extends IControlConfig {}
    interface SliderControlConfig extends IControlConfig {}
    interface LogoControlConfig extends IControlConfig {}
// @formatter:on
}
