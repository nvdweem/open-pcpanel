package dev.niels.pcpanel.core.device.light;

import dev.niels.pcpanel.core.device.Device;
import dev.niels.pcpanel.core.device.light.control.ControlConfig;
import dev.niels.pcpanel.core.device.light.control.IControlConfig;
import dev.niels.pcpanel.core.device.light.control.StaticConfig;
import dev.niels.pcpanel.core.helper.ByteArrayBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import one.util.streamex.IntStreamEx;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CustomLightConfig extends LightConfig {
  private List<ControlConfig> knobs = new ArrayList<>();
  private List<ControlConfig> sliderLabels = new ArrayList<>();
  private List<ControlConfig> sliders = new ArrayList<>();
  private ControlConfig logo;

  public static CustomLightConfig build(Device device) {
    int knobs = device.getButtonCount();
    int sliders = device.getAnalogCount() - knobs;

    return new CustomLightConfig()
      .setKnobs(IntStreamEx.range(knobs).mapToObj(x -> new StaticConfig()).select(ControlConfig.class).toList())
      .setSliderLabels(IntStreamEx.range(sliders).mapToObj(x -> new StaticConfig()).select(ControlConfig.class).toList())
      .setSliders(IntStreamEx.range(sliders).mapToObj(x -> new StaticConfig()).select(ControlConfig.class).toList())
      .setLogo(new StaticConfig());
  }

  public CustomLightConfig setKnob(int idx, IControlConfig.KnobControlConfig config) {
    knobs.set(idx, config.toConfig());
    return this;
  }

  public CustomLightConfig setSliderLabel(int idx, StaticConfig config) {
    sliderLabels.set(idx, config);
    return this;
  }

  public CustomLightConfig setSlider(int idx, IControlConfig.SliderControlConfig config) {
    sliders.set(idx, config.toConfig());
    return this;
  }

  @Override
  public byte[][] toCommand() {
    return new byte[][]{
      addAll(knobs, 5, 2),
      addAll(sliderLabels, 5, 1),
      addAll(sliders, 5, 0),
      addAll(List.of(logo), 5, 3),
    };
  }

  private byte[] addAll(List<ControlConfig> cfgs, int... prefix) {
    var bab = new ByteArrayBuilder(prefix);
    cfgs.forEach(c -> c.appendToBuilder(bab));
    return bab.getBytes();
  }
}
