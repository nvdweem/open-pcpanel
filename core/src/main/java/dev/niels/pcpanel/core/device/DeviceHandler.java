package dev.niels.pcpanel.core.device;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DeviceHandler {
  @EventListener
  public void controlTouched(DeviceControlEvent event) {
    log.info("{}", event);
  }
}
