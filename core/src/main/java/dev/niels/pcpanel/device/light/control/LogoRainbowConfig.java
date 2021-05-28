package dev.niels.pcpanel.device.light.control;

import dev.niels.pcpanel.helper.ByteArrayBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LogoRainbowConfig extends ControlConfig implements IControlConfig.LogoControlConfig {
    private int brightness;
    private int speed;

    @Override
    public void doAppend(ByteArrayBuilder builder) {
        builder.append(2, -1, brightness, speed);
    }
}
