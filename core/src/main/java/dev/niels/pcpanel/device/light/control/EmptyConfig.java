package dev.niels.pcpanel.device.light.control;

import dev.niels.pcpanel.helper.ByteArrayBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class EmptyConfig extends ControlConfig implements IControlConfig, IControlConfig.KnobControlConfig, IControlConfig.SliderControlConfig, IControlConfig.SliderLabelControlConfig, IControlConfig.LogoControlConfig {
    @Override
    public void doAppend(ByteArrayBuilder builder) {
    }
}
