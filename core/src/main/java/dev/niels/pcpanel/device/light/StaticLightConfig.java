package dev.niels.pcpanel.device.light;

import dev.niels.pcpanel.helper.ByteArrayBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.awt.Color;

@Data
@EqualsAndHashCode(callSuper = true)
public class StaticLightConfig extends LightConfig {
    private Color color;

    @Override
    public byte[][] toCommand() {
        return new byte[][]{new ByteArrayBuilder(5, 4, 2).append(color).getBytes()};
    }
}