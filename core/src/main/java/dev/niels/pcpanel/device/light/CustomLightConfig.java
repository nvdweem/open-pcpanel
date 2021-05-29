package dev.niels.pcpanel.device.light;

import dev.niels.pcpanel.device.Device;
import dev.niels.pcpanel.device.light.control.EmptyConfig;
import dev.niels.pcpanel.device.light.control.IControlConfig;
import dev.niels.pcpanel.device.light.control.StaticConfig;
import dev.niels.pcpanel.helper.ByteArrayBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import one.util.streamex.IntStreamEx;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CustomLightConfig extends LightConfig {
    private List<IControlConfig.KnobControlConfig> knobConfigs = new ArrayList<>();
    private List<IControlConfig> sliderLabels = new ArrayList<>();
    private List<IControlConfig.SliderControlConfig> sliders = new ArrayList<>();
    private IControlConfig.LogoControlConfig logo;

    public static CustomLightConfig build(Device device) {
        int knobs = device.getButtonCount();
        int sliders = device.getAnalogCount() - knobs;

        return new CustomLightConfig()
                .setKnobConfigs(IntStreamEx.range(knobs).mapToObj(x -> new EmptyConfig()).select(IControlConfig.KnobControlConfig.class).toList())
                .setSliderLabels(IntStreamEx.range(sliders).mapToObj(x -> new EmptyConfig()).select(IControlConfig.class).toList())
                .setSliders(IntStreamEx.range(sliders).mapToObj(x -> new EmptyConfig()).select(IControlConfig.SliderControlConfig.class).toList())
                .setLogo(new EmptyConfig());
    }

    public CustomLightConfig setKnobConfig(int idx, IControlConfig.KnobControlConfig config) {
        knobConfigs.set(idx, config);
        return this;
    }

    public CustomLightConfig setSliderLabel(int idx, StaticConfig config) {
        sliderLabels.set(idx, config);
        return this;
    }

    public CustomLightConfig setSlider(int idx, IControlConfig.SliderControlConfig config) {
        sliders.set(idx, config);
        return this;
    }

    @Override
    public byte[][] toCommand() {
        return new byte[][]{
                addAll(knobConfigs, 5, 2),
                addAll(sliderLabels, 5, 1),
                addAll(sliders, 5, 0),
                addAll(List.of(logo), 5, 3),
        };
    }

    private <T extends IControlConfig> byte[] addAll(List<T> cfgs, int... prefix) {
        var bab = new ByteArrayBuilder(prefix);
        cfgs.forEach(c -> c.appendToBuilder(bab));
        return bab.getBytes();
    }
}