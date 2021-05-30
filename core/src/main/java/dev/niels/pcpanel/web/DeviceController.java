package dev.niels.pcpanel.web;

import dev.niels.pcpanel.JsonColor;
import dev.niels.pcpanel.device.ConnectedDevice;
import dev.niels.pcpanel.device.ConnectedDeviceService;
import dev.niels.pcpanel.device.light.BreathLightConfig;
import dev.niels.pcpanel.device.light.CustomLightConfig;
import dev.niels.pcpanel.device.light.LightConfig;
import dev.niels.pcpanel.device.light.RainbowLightConfig;
import dev.niels.pcpanel.device.light.StaticLightConfig;
import dev.niels.pcpanel.device.light.WaveLightConfig;
import dev.niels.pcpanel.device.light.control.ControlConfig;
import dev.niels.pcpanel.device.light.control.EmptyConfig;
import dev.niels.pcpanel.device.light.control.GradientConfig;
import dev.niels.pcpanel.device.light.control.IControlConfig;
import dev.niels.pcpanel.device.light.control.LogoBreathConfig;
import dev.niels.pcpanel.device.light.control.LogoRainbowConfig;
import dev.niels.pcpanel.device.light.control.StaticConfig;
import dev.niels.pcpanel.device.light.control.VolumeGradientConfig;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.awt.Color;
import java.util.Collection;

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
      device.setConfig(buildBodyCfg(lcr));
    } else {
      setControlLight(device, lcr);
    }

    return true;
  }

  private void setControlLight(ConnectedDevice device, LightChangeRequest lcr) {
    var cfg = CustomLightConfig.build(device.getType());
    var controlConfig = buildCfg(lcr);

    var idx = lcr.getIdx() - 1;
    switch (lcr.getControl()) {
      case "knob":
        cfg.setKnobConfig(idx, (IControlConfig.KnobControlConfig) controlConfig);
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

    device.setConfig(cfg);
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
    return new EmptyConfig();
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
    return (int) Math.floor(Color.RGBtoHSB(clr.getRed(), clr.getGreen(), clr.getBlue(), null)[0] * 256);
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
}
