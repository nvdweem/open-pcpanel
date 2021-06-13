package pcpanel.dev.niels.pcpanel;

import one.util.streamex.StreamEx;
import pcpanel.dev.niels.pcpanel.natives.VolumeControlService;
import pcpanel.dev.niels.pcpanel.natives.VolumeDevice;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public abstract class Util {
  public static int translateAnalog(int slider, boolean log, Integer trimMin, Integer trimMax) {
    var pos = Util.map(slider, 0, 255, 0, 100);
    if (log) {
      trimMin = trimMin == null ? 0 : trimMin;
      trimMax = trimMax == null ? 0 : trimMax;
      return Util.map(Util.log(pos), 0, 100, trimMin, trimMax);
    }
    return pos;
  }

  public static int log(int x) {
    double cons = 21.6679065336D;
    double ans = Math.pow(Math.E, x / cons) - 1.0D;
    return (int) Math.round(ans);
  }

  public static int map(int x, int in_min, int in_max, int out_min, int out_max) {
    return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
  }

  public static String determineDevice(String which, String specific, String whichDefault) {
    if ("Specific".equals(which)) {
      return specific;
    }
    var type = Optional.ofNullable(whichDefault).filter(v -> !v.endsWith("out")).map(v -> VolumeDevice.DeviceType.capture).orElse(VolumeDevice.DeviceType.render);
    var role = Optional.ofNullable(whichDefault).filter(v -> !v.startsWith("Multimedia")).map(v -> Set.of(VolumeDevice.DeviceRole.communications)).orElse(Set.of(VolumeDevice.DeviceRole.multimedia, VolumeDevice.DeviceRole.console));
    return StreamEx.of(VolumeControlService.getDevices().values())
      .filterBy(VolumeDevice::getType, type)
      .remove(d -> Collections.disjoint(role, d.getDefaultFor()))
      .map(VolumeDevice::getId)
      .findFirst().orElse(null);
  }
}
