package dev.niels.pcpanel.core.web;

import dev.niels.pcpanel.core.JsonColor;
import dev.niels.pcpanel.core.SpringContext;
import dev.niels.pcpanel.core.device.ConnectedDevice;
import dev.niels.pcpanel.core.device.ConnectedDeviceService;
import dev.niels.pcpanel.core.device.light.BreathLightConfig;
import dev.niels.pcpanel.core.device.light.CustomLightConfig;
import dev.niels.pcpanel.core.device.light.LightConfig;
import dev.niels.pcpanel.core.device.light.RainbowLightConfig;
import dev.niels.pcpanel.core.device.light.StaticLightConfig;
import dev.niels.pcpanel.core.device.light.WaveLightConfig;
import dev.niels.pcpanel.core.device.light.control.ControlConfig;
import dev.niels.pcpanel.core.device.light.control.GradientConfig;
import dev.niels.pcpanel.core.device.light.control.IControlConfig;
import dev.niels.pcpanel.core.device.light.control.LogoBreathConfig;
import dev.niels.pcpanel.core.device.light.control.LogoRainbowConfig;
import dev.niels.pcpanel.core.device.light.control.StaticConfig;
import dev.niels.pcpanel.core.device.light.control.VolumeGradientConfig;
import dev.niels.pcpanel.core.profile.Actions;
import dev.niels.pcpanel.plugins.Action;
import dev.niels.pcpanel.plugins.AnalogAction;
import dev.niels.pcpanel.plugins.config.ActionConfig;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.TriConsumer;
import org.springframework.data.util.ReflectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.awt.Color;
import java.util.Collection;
import java.util.Map;

import static org.springframework.util.ReflectionUtils.doWithFields;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DeviceController {
  private final ConnectedDeviceService deviceService;

  @GetMapping("devices")
  public Collection<ConnectedDevice> getConnectedDevices() {
    return deviceService.getDevices();
  }

  @PostMapping("changelight")
  public boolean changeLight(@RequestBody LightChangeRequest lcr) {
    var device = deviceService.getDevice(lcr.getDevice()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));

    if (lcr.control.equals("body")) {
      device.setActiveProfile(device.getActiveProfile().setLightConfig(buildBodyCfg(lcr)));
    } else {
      setControlLight(device, lcr);
    }

    return true;
  }

  private boolean changeControl(AnalogRequest ar, TriConsumer<Actions, Integer, ActionConfig> setter) throws Exception {
    var device = deviceService.getDevice(ar.getDevice()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));

    Action<?> aa = SpringContext.getBean((Class<AnalogAction<?>>) Class.forName((String) ar.getParams().get("__type")));
    var obj = aa.getConfigurationClass().getConstructor().newInstance();
    doWithFields(aa.getConfigurationClass(), f -> {
      var value = ar.getParams().get(f.getName());
      if (value != null) {
        ReflectionUtils.setField(f, obj, value);
      }
    });

    var idx = ar.getIdx() - 1 + (ar.getControl().equals("slider") ? device.getType().getButtonCount() : 0);
    setter.accept(device.getActiveProfile().getActionsConfig(), idx, (ActionConfig) obj);
    return true;
  }

  @PostMapping("changeknob")
  public boolean changeKnob(@RequestBody AnalogRequest ar) throws Exception {
    return changeControl(ar, Actions::setKnobAction);
  }

  @PostMapping("changeanalog")
  public boolean changeAnalog(@RequestBody AnalogRequest ar) throws Exception {
    return changeControl(ar, Actions::setAnalogAction);
  }

  private void setControlLight(ConnectedDevice device, LightChangeRequest lcr) {
    var profile = device.getActiveProfile();
    var currentConfig = profile.getLightConfig();
    if (!(currentConfig instanceof CustomLightConfig)) {
      currentConfig = CustomLightConfig.build(device.getType());
      profile.setLightConfig(currentConfig);
    }
    var cfg = (CustomLightConfig) currentConfig;

    var controlConfig = buildCfg(lcr);
    var idx = lcr.getIdx() - 1;
    switch (lcr.getControl()) {
      case "knob":
        cfg.setKnob(idx, (IControlConfig.KnobControlConfig) controlConfig);
        break;
      case "slider":
        cfg.setSlider(idx, (IControlConfig.SliderControlConfig) controlConfig);
        break;
      case "slider-label":
        cfg.setSliderLabel(idx, (StaticConfig) controlConfig);
        break;
      case "logo":
        cfg.setLogo(controlConfig);
        break;
    }

    device.setActiveProfile(profile);
  }

  private ControlConfig buildCfg(LightChangeRequest lcr) {
    var color1 = lcr.getColor1();
    var color2 = lcr.getColor2();
    switch (lcr.getType()) {
      case "static":
        return new StaticConfig().setColors(color1);
      case "wave":
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Wave and breath are not supported for controls");
      case "breath":
        return new LogoBreathConfig().setHue(hueFrom(color1)).setBrightness(lcr.getBrightness()).setSpeed(lcr.getSpeed());
      case "rainbow":
        return new LogoRainbowConfig().setBrightness(lcr.getBrightness()).setSpeed(lcr.getSpeed());
      case "volumeGradient":
        if (lcr.getControl().equals("knob")) {
          return new GradientConfig().setColor1(color1).setColor2(color2);
        } else {
          return new VolumeGradientConfig().setColor1(color1).setColor2(color2);
        }
      case "gradient":
        return new GradientConfig().setColor1(color1).setColor2(color2);
    }
    return new StaticConfig().setColors(Color.black);
  }

  private LightConfig buildBodyCfg(LightChangeRequest lcr) {
    var color = lcr.getColor1();
    switch (lcr.type) {
      case "static":
        return new StaticLightConfig().setColor(color);
      case "wave":
        return new WaveLightConfig().setHue(hueFrom(color)).setBrightness(lcr.getBrightness()).setSpeed(lcr.getSpeed()).setReverse(lcr.isReverse()).setBounce(lcr.isBounce());
      case "rainbow":
        return new RainbowLightConfig().setPhaseShift(lcr.getPhaseShift()).setBrightness(lcr.getBrightness()).setSpeed(lcr.getSpeed()).setReverse(lcr.isReverse());
      case "breath":
        return new BreathLightConfig().setHue(hueFrom(color)).setBrightness(lcr.getBrightness()).setSpeed(lcr.getSpeed());
      default:
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to determine type for " + lcr.type);
    }
  }

  private int hueFrom(Color clr) {
    return clr == null ? 0 : (int) Math.floor(Color.RGBtoHSB(clr.getRed(), clr.getGreen(), clr.getBlue(), null)[0] * 256);
  }

  @Data
  public static class LightChangeRequest {
    private String device;
    private String control;
    private int idx;
    private String type;
    @JsonColor private Color color1;
    @JsonColor private Color color2;
    private int brightness;
    private int speed;
    private int phaseShift;
    private boolean reverse;
    private boolean bounce;
  }

  @Data
  public static class AnalogRequest {
    private String device;
    private String control;
    private int idx;
    private Map<String, Object> params;
  }
}
