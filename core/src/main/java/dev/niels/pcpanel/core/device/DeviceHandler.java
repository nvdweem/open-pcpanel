package dev.niels.pcpanel.core.device;

import dev.niels.pcpanel.core.SpringContext;
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
      action.triggerAction(config, event.getValue());
    }
  }
}
