package dev.niels.pcpanel.device.light;

import dev.niels.pcpanel.helper.ByteArrayBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BreathLightConfig extends LightConfig {
    private int hue;
    private int brightness;
    private int speed;

    @Override
    public byte[][] toCommand() {
        return new byte[][]{new ByteArrayBuilder(5, 4, 4, hue, -1, brightness, speed).getBytes()};
    }
}
