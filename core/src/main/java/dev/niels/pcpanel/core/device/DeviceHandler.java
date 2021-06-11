package dev.niels.pcpanel.core.device;

import dev.niels.pcpanel.core.SpringContext;
import dev.niels.pcpanel.core.device.light.CustomLightConfig;
import dev.niels.pcpanel.core.device.light.control.GradientConfig;
import dev.niels.pcpanel.core.device.light.control.IControlConfig;
import dev.niels.pcpanel.core.device.light.control.StaticConfig;
import dev.niels.pcpanel.core.device.light.control.VolumeGradientConfig;
import dev.niels.pcpanel.core.profile.Actions;
import dev.niels.pcpanel.core.profile.ProfileRepository;
import dev.niels.pcpanel.plugins.Action;
import dev.niels.pcpanel.plugins.AnalogAction;
import dev.niels.pcpanel.plugins.KnobAction;
import dev.niels.pcpanel.plugins.config.ActionConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.EntryStream;
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
        var action = getAction(config, KnobAction.class);
        if (event.getValue() == 1) {
          action.buttonDown(buildWrapper(event), config);
        } else {
          action.buttonUp(buildWrapper(event), config);
        }
        break;
      }
      case knobRotate: {
        var config = actionConfig.getAnalogActions().get(event.getKey());
        var action = getAction(config, AnalogAction.class);
        action.triggerAction(buildWrapper(event), config, event.getValue());
        break;
      }
    }
  }

  @EventListener
  public void connectedDeviceEvent(ConnectedDeviceEvent event) {
    var actions = event.getDevice().getActiveProfile().getActionsConfig();
    if (event.getControlIdx() == null) {
      EntryStream.of(actions.getKnobActions()).nonNullKeys().mapKeyValue((i, cfg) -> new ConnectedDeviceEvent(event.getDevice()).setControlIdx(i).setKnobAction(true))
        .append(EntryStream.of(actions.getAnalogActions()).nonNullKeys().mapKeyValue((i, cfg) -> new ConnectedDeviceEvent(event.getDevice()).setControlIdx(i).setKnobAction(false)))
        .forEach(this::initControl);
    } else {
      initControl(event);
    }
  }

  private void initControl(ConnectedDeviceEvent event) {
    Actions actions = event.getDevice().getActiveProfile().getActionsConfig();
    ActionConfig cfg;
    Class<? extends Action> clz;
    DeviceControlEvent.Type type;
    if (event.isKnobAction()) {
      cfg = actions.getKnobActions().get(event.getControlIdx());
      clz = KnobAction.class;
      type = DeviceControlEvent.Type.knobPressed;
    } else {
      cfg = actions.getAnalogActions().get(event.getControlIdx());
      clz = AnalogAction.class;
      type = DeviceControlEvent.Type.knobRotate;
    }
    getAction(cfg, clz).init(buildWrapper(new DeviceControlEvent(event.getDevice(), type, event.getControlIdx(), 0)), cfg);
  }

  private <T extends Action<ActionConfig>> T getAction(ActionConfig config, Class<T> typeClass) {
    var clazz = config.getActionClass().asSubclass(typeClass);
    return SpringContext.getBean(clazz);
  }

  private ControlWrapper buildWrapper(DeviceControlEvent event) {
    var isSlider = event.getKey() > event.getConnectedDevice().getType().getButtonCount();
    var idx = isSlider ? event.getKey() - event.getConnectedDevice().getType().getButtonCount() : event.getKey();
    var config = event.getConnectedDevice().getCurrentLights();
    var originalConfig = event.getConnectedDevice().getActiveProfile().getLightConfig();

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
