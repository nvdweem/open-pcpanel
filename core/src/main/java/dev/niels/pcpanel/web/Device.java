package dev.niels.pcpanel.web;

import dev.niels.pcpanel.device.ConnectedDevice;
import dev.niels.pcpanel.device.ConnectedDeviceService;
import dev.niels.pcpanel.device.light.BreathLightConfig;
import dev.niels.pcpanel.device.light.LightConfig;
import dev.niels.pcpanel.device.light.RainbowLightConfig;
import dev.niels.pcpanel.device.light.StaticLightConfig;
import dev.niels.pcpanel.device.light.WaveLightConfig;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.awt.Color;
import java.util.Collection;
import java.util.regex.Pattern;

@Slf4j
@RestController
@Scope("prototype")
@RequiredArgsConstructor
public class Device {
    private static final Pattern rgbPattern = Pattern.compile("rgb\\((\\d+), (\\d+), (\\d+)\\)");
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
        }

        return true;
    }

    private LightConfig buildBodyCfg(LightChangeRequest lcr) {
        var color = parseColor(lcr.getColor1());
        switch (lcr.type) {
            case "static":
                return new StaticLightConfig().setColor(color);
            case "wave": {
                return new WaveLightConfig().setHue(hueFrom(color)).setBrightness(lcr.getBrightness()).setSpeed(lcr.getSpeed()).setReverse(lcr.isReverse()).setBounce(lcr.isBounce());
            }
            case "rainbow":
                return new RainbowLightConfig().setPhaseShift(lcr.getPhaseShift()).setBrightness(lcr.getBrightness()).setSpeed(lcr.getSpeed()).setReverse(lcr.isReverse());
            case "breath": {
                return new BreathLightConfig().setHue(hueFrom(color)).setBrightness(lcr.getBrightness()).setSpeed(lcr.getSpeed());
            }
            default:
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to determine type for " + lcr.type);
        }
    }

    private int hueFrom(Color clr) {
        return (int) Math.floor(Color.RGBtoHSB(clr.getRed(), clr.getGreen(), clr.getBlue(), null)[0] * 256);
    }


    private Color parseColor(String color) {
        if (StringUtils.hasText(color)) {
            var m = rgbPattern.matcher(color);
            if (m.find()) {
                return new Color(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)));
            }
            try {
                return Color.decode(color);
            } catch (Exception e) {
                // Not rgb
            }
        }
        return Color.white;
    }

    @Data
    public static class LightChangeRequest {
        private String device;
        private String control;
        private int idx;
        private String type;
        private String color1;
        private String color2;
        private int brightness;
        private int speed;
        private int phaseShift;
        private boolean reverse;
        private boolean bounce;
    }
}
