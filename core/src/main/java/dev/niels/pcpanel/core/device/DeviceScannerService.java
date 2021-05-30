package dev.niels.pcpanel.core.device;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hid4java.HidDevice;
import org.hid4java.HidManager;
import org.hid4java.HidServicesListener;
import org.hid4java.HidServicesSpecification;
import org.hid4java.ScanMode;
import org.hid4java.event.HidServicesEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceScannerService implements HidServicesListener {
  private final ApplicationEventPublisher eventPublisher;

  @Scheduled(initialDelay = 0L, fixedDelay = Long.MAX_VALUE)
  private void init() {
    var result = HidManager.getHidServices(buildSpecification());
    result.addHidServicesListener(this);
    log.info("Starting HID services");
    result.start();
    result.getAttachedHidDevices().forEach(this::connect);
  }

  private void connect(HidDevice hidDevice) {
    var panel = Device.getPanelType(hidDevice);
    if (panel.isPresent()) {
      log.debug("Device connected: {}", hidDevice);
      eventPublisher.publishEvent(new DeviceEvent(true, hidDevice));
    }
  }

  private HidServicesSpecification buildSpecification() {
    var result = new HidServicesSpecification();
    result.setAutoShutdown(false);
    result.setScanInterval(3000);
    result.setPauseInterval(2000);
    result.setScanMode(ScanMode.SCAN_AT_FIXED_INTERVAL);
    return result;
  }

  @Override public void hidDeviceAttached(HidServicesEvent event) {
    log.debug("Device attached {}", event);
    connect(event.getHidDevice());
  }

  @Override public void hidDeviceDetached(HidServicesEvent event) {
    log.debug("Device detached {}", event);
    if (Device.getPanelType(event.getHidDevice()).isPresent()) {
      eventPublisher.publishEvent(new DeviceEvent(false, event.getHidDevice()));
    }
  }

  @Override public void hidFailure(HidServicesEvent event) {
    log.info("Device failure {}", event);
  }
}
