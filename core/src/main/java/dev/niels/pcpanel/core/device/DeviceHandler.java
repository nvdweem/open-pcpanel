package dev.niels.pcpanel.core.device;

import dev.niels.pcpanel.core.SpringContext;
import dev.niels.pcpanel.core.device.light.CustomLightConfig;
import dev.niels.pcpanel.core.device.light.control.GradientConfig;
import dev.niels.pcpanel.core.device.light.control.StaticConfig;
import dev.niels.pcpanel.core.device.light.control.VolumeGradientConfig;
import dev.niels.pcpanel.plugins.AnalogAction;
import dev.niels.pcpanel.plugins.config.ActionConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceHandler {
  @EventListener
  public void controlTouched(DeviceControlEvent event) {
    log.trace("{}", event);

    if (event.getType() == DeviceControlEvent.Type.knobRotate) {
      var config = event.getConnectedDevice().getActiveProfile().getActionsConfig().getAnalogActions().get(event.getKey());
      var action = (AnalogAction<ActionConfig>) SpringContext.getBean(config.getActionClass());
      action.triggerAction(buildWrapper(event), config, event.getValue());
    }
  }

  private ControlWrapper buildWrapper(DeviceControlEvent event) {
    var isSlider = event.getKey() > event.getConnectedDevice().getType().getButtonCount();
    var idx = isSlider ? event.getKey() - event.getConnectedDevice().getType().getButtonCount() : event.getKey();
    var config = event.getConnectedDevice().getActiveProfile().getLightConfig();
    ControlWrapper.SingleColorSetter scs;
    ControlWrapper.TwoColorSetter tcs;
    if (config instanceof CustomLightConfig) {
      scs = color -> {
        if (isSlider) {
          ((CustomLightConfig) config).setSlider(idx, new StaticConfig().setColor1(color));
        } else {
          ((CustomLightConfig) config).setKnob(idx, new StaticConfig().setColor1(color));
        }
        event.getConnectedDevice().sendCurrentConfig();
      };
      tcs = (color1, color2) -> {
        if (isSlider) {
          ((CustomLightConfig) config).setSlider(idx, new VolumeGradientConfig().setColor1(color1).setColor2(color2));
        } else {
          ((CustomLightConfig) config).setKnob(idx, new GradientConfig().setColor1(color1).setColor2(color2));
        }
        event.getConnectedDevice().sendCurrentConfig();
      };
    } else {
      scs = color -> {
      };
      tcs = (a, b) -> {
      };
    }
    var control = new ControlWrapper(isSlider, scs, tcs);
    return control;
  }
}
