package dev.niels.pcpanel.device.light;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.niels.pcpanel.core.device.Device;
import dev.niels.pcpanel.core.device.light.BreathLightConfig;
import dev.niels.pcpanel.core.device.light.CustomLightConfig;
import dev.niels.pcpanel.core.device.light.LightConfig;
import dev.niels.pcpanel.core.device.light.RainbowLightConfig;
import dev.niels.pcpanel.core.device.light.StaticLightConfig;
import dev.niels.pcpanel.core.device.light.WaveLightConfig;
import dev.niels.pcpanel.core.device.light.control.GradientConfig;
import dev.niels.pcpanel.core.device.light.control.LogoBreathConfig;
import dev.niels.pcpanel.core.device.light.control.StaticConfig;
import dev.niels.pcpanel.core.device.light.control.VolumeGradientConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JacksonLightConfigTest {
  private ObjectMapper om;

  @BeforeEach
  void setUp() {
    om = new ObjectMapper();
  }

  @Test
  void testAll() throws JsonProcessingException {
    var strs = Map.of(
      BreathLightConfig.class, om.writeValueAsString(new BreathLightConfig().setBrightness(1).setHue(2).setSpeed(3)),
      RainbowLightConfig.class, om.writeValueAsString(new RainbowLightConfig().setBrightness(1).setSpeed(3).setReverse(true).setPhaseShift(4)),
      StaticLightConfig.class, om.writeValueAsString(new StaticLightConfig().setColor(Color.green)),
      WaveLightConfig.class, om.writeValueAsString(new WaveLightConfig().setBrightness(1).setHue(2).setSpeed(3).setReverse(true).setBounce(true))
    );

    for (var entry : strs.entrySet()) {
      assertEquals(entry.getKey(), om.readValue(entry.getValue(), LightConfig.class).getClass());
    }
  }

  @Test
  void testCustom() throws JsonProcessingException {
    var customCfg = CustomLightConfig.build(Device.PCPANEL_PRO).setKnob(0, new StaticConfig()).setKnob(1, new GradientConfig().setColor1(Color.green).setColor2(Color.red)).setKnob(2, new StaticConfig().setColors(Color.red))
      .setSlider(0, new GradientConfig().setColor1(Color.green).setColor2(Color.red)).setSlider(1, new StaticConfig().setColors(Color.red)).setSlider(2, new VolumeGradientConfig().setColor1(Color.green).setColor2(Color.red))
      .setSliderLabel(0, new StaticConfig().setColors(Color.green)).setLogo(new LogoBreathConfig().setBrightness(1).setSpeed(2).setHue(5));

    var readCfg = om.readValue(om.writeValueAsString(customCfg), LightConfig.class);
    assertEquals(customCfg.toString(), readCfg.toString());
  }
}
