package dev.niels.pcpanel.core.device;

import dev.niels.pcpanel.core.SpringContext;
import dev.niels.pcpanel.core.device.light.CustomLightConfig;
import dev.niels.pcpanel.core.device.light.control.GradientConfig;
import dev.niels.pcpanel.core.device.light.control.IControlConfig;
import dev.niels.pcpanel.core.device.light.control.StaticConfig;
import dev.niels.pcpanel.core.device.light.control.VolumeGradientConfig;
import dev.niels.pcpanel.core.profile.Profile;
import dev.niels.pcpanel.core.profile.ProfileRepository;
import dev.niels.pcpanel.plugins.AnalogAction;
import dev.niels.pcpanel.plugins.KnobAction;
import dev.niels.pcpanel.plugins.config.ActionConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceHandler {
  private final ProfileRepository pr;

  @EventListener
  public void controlTouched(DeviceControlEvent event) {
    log.trace("{}", event);

    var actionConfig = event.getConnectedDevice().getActiveProfile().getActionsConfig();
    switch (event.getType()) {
      case knobPressed: {
        var config = actionConfig.getKnobActions().get(event.getKey());
        var clazz = config.getActionClass().asSubclass(KnobAction.class);
        KnobAction<ActionConfig> action = SpringContext.getBean(clazz);
        if (event.getValue() == 1) {
          action.buttonDown(buildWrapper(event), config);
        } else {
          action.buttonUp(buildWrapper(event), config);
        }
        break;
      }
      case knobRotate: {
        var config = actionConfig.getAnalogActions().get(event.getKey());
        var clazz = config.getActionClass().asSubclass(AnalogAction.class);
        AnalogAction<ActionConfig> action = SpringContext.getBean(clazz);
        action.triggerAction(buildWrapper(event), config, event.getValue());
        break;
      }
    }
  }

  private ControlWrapper buildWrapper(DeviceControlEvent event) {
    var isSlider = event.getKey() > event.getConnectedDevice().getType().getButtonCount();
    var idx = isSlider ? event.getKey() - event.getConnectedDevice().getType().getButtonCount() : event.getKey();
    var activeProfile = event.getConnectedDevice().getActiveProfile();
    var config = activeProfile.getLightConfig();
    var originalConfig = pr.findById(activeProfile.getId()).map(Profile::init).orElse(activeProfile).getLightConfig();

    ControlWrapper.SingleColorSetter scs;
    ControlWrapper.TwoColorSetter tcs;
    if (config instanceof CustomLightConfig) {
      scs = color -> {
        if (isSlider) {
          ((CustomLightConfig) config).setSlider(idx, color != null ? new StaticConfig().setColor1(color) : (IControlConfig.SliderControlConfig) ((CustomLightConfig) originalConfig).getSliders().get(idx));
        } else {
          ((CustomLightConfig) config).setKnob(idx, color != null ? new StaticConfig().setColor1(color) : (IControlConfig.KnobControlConfig) ((CustomLightConfig) originalConfig).getKnobs().get(idx));
        }
        event.getConnectedDevice().sendCurrentConfig();
      };
      tcs = (color1, color2) -> {
        if (isSlider) {
          ((CustomLightConfig) config).setSlider(idx, color1 != null && color2 != null ? new VolumeGradientConfig().setColor1(color1).setColor2(color2) : (IControlConfig.SliderControlConfig) ((CustomLightConfig) originalConfig).getSliders().get(idx));
        } else {
          ((CustomLightConfig) config).setKnob(idx, color1 != null && color2 != null ? new GradientConfig().setColor1(color1).setColor2(color2) : (IControlConfig.KnobControlConfig) ((CustomLightConfig) originalConfig).getSliders().get(idx));
        }
        event.getConnectedDevice().sendCurrentConfig();
      };
    } else {
      scs = color -> {
      };
      tcs = (a, b) -> {
      };
    }
    return new ControlWrapper(isSlider, scs, tcs);
  }
}
