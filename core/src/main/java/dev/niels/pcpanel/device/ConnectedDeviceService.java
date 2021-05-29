package dev.niels.pcpanel.device;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Order(0)
@Service
@RequiredArgsConstructor
public class ConnectedDeviceService {
    private final ObjectFactory<ConnectedDevice> connectedDeviceFactory;
    private final Map<String, ConnectedDevice> devices = new HashMap<>();

    @EventListener
    public void deviceConnected(DeviceEvent event) {
        var deviceId = event.getDevice().getSerialNumber();
        if (event.isConnected()) {
            devices.put(deviceId, connectedDeviceFactory.getObject().init(event.getDevice()));
        } else {
            devices.remove(deviceId);
        }
    }

    public Collection<ConnectedDevice> getDevices() {
        return new ArrayList<>(devices.values());
    }

    public Optional<ConnectedDevice> getDevice(String id) {
        return Optional.ofNullable(devices.get(id));
    }
}
