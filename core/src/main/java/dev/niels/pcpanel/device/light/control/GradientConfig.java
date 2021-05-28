package dev.niels.pcpanel.device.light.control;

import dev.niels.pcpanel.helper.ByteArrayBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.awt.Color;

@Data
@EqualsAndHashCode(callSuper = true)
public class GradientConfig extends ControlConfig implements IControlConfig.KnobControlConfig {
    private Color color1;
    private Color color2;

    @Override
    public void doAppend(ByteArrayBuilder builder) {
        builder.append(2).append(color1).append(color2);
    }
}
