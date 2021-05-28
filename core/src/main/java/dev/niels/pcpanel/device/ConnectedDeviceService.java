package dev.niels.pcpanel.device;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Slf4j
@Order(0)
@Service
@RequiredArgsConstructor
public class ConnectedDeviceService {
    private final ObjectFactory<ConnectedDevice> connectedDeviceFactory;

    @EventListener
    public void deviceConnected(DeviceEvent event) {
        if (event.isConnected()) {
            connectedDeviceFactory.getObject().init(event.getDevice());
        }
    }
}
