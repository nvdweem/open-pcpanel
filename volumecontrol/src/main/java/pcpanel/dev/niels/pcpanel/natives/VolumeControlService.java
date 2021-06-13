package pcpanel.dev.niels.pcpanel.natives;

import com.sun.jna.WString;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VolumeControlService {
  @Getter private static Map<String, VolumeDevice> devices = new HashMap<>();
  @Getter private static Map<Long, VolumeSession> sessions = new HashMap<>();

  @PostConstruct
  public void construct() {
    VolumeControlLib.INSTANCE.initialize(
      VolumeControlService::deviceChanged, name -> devices.remove(name.toString()),
      VolumeControlService::sessionChanged, pid -> sessions.remove(pid),
      VolumeControlService::defaultDeviceChanged,
      d -> log.debug("{}", d), i -> log.info("{}", i)
    );
  }

  private static void deviceChanged(WString name, WString id, int volume, int muted, int type) {
    var device = devices.computeIfAbsent(id.toString(), VolumeDevice::new);
    if (name.length() != 0) {
      device.setName(name.toString());
    }
    device.setVolume(volume)
      .setMuted(muted != 0)
      .setType(VolumeDevice.DeviceType.fromInt(type));
  }

  private static void sessionChanged(long pid, WString process, WString icon, int volume, int muted) {
    var session = sessions.computeIfAbsent(pid, VolumeSession::new);
    session.setProcess(process.toString());
    session.setIcon(icon.toString());
    session.setVolume(volume);
    session.setMuted(muted != 0);
  }

  private static void defaultDeviceChanged(WString id, int type, int role) {
    var t = VolumeDevice.DeviceType.fromInt(type);
    var r = VolumeDevice.DeviceRole.fromInt(role);
    StreamEx.of(devices.values()).filterBy(VolumeDevice::getType, t)
      .forEach(device -> {
        device.getDefaultFor().remove(r);
        if (id.toString().equals(device.getId())) {
          device.getDefaultFor().add(r);
        }
      });
  }

  /* Implementations of VolumeControlLib functions */
  public void setDeviceVolume(String id, int volume, boolean osd) {
    VolumeControlLib.INSTANCE.setDeviceVolume(new WString(id), volume, osd ? 1 : 0);
  }

  public void setProcessVolume(String name, int volume, boolean osd) {
    VolumeControlLib.INSTANCE.setProcessVolume(new WString(name), volume, osd ? 1 : 0);
  }

  public void setFgProcessVolume(int volume, boolean osd) {
    VolumeControlLib.INSTANCE.setFgProcessVolume(volume, osd ? 1 : 0);
  }

  public void setDeviceMute(String id, boolean muted, boolean osd) {
    VolumeControlLib.INSTANCE.setDeviceMute(new WString(id), muted ? 1 : 0, osd ? 1 : 0);
  }

  public boolean toggleDeviceMute(String device, boolean osd) {
    var dev = devices.get(device);
    if (dev != null) {
      var muted = !dev.isMuted();
      setDeviceMute(device, muted, osd);
      return muted;
    }
    return false;
  }

  public void setProcessMute(String name, boolean muted, boolean osd) {
    VolumeControlLib.INSTANCE.setProcessMute(new WString(name), muted ? 1 : 0, osd ? 1 : 0);
  }

  public boolean toggleProcessMute(String name, boolean osd) {
    var muted = !StreamEx.of(sessions.values()).findFirst(s -> s.getProcess().toLowerCase().contains(name.toLowerCase())).map(VolumeSession::isMuted).orElse(true);
    setProcessMute(name, muted, osd);
    return muted;
  }

  public void setActiveDevice(String id, VolumeDevice.DeviceRole role, boolean osd) {
    VolumeControlLib.INSTANCE.setActiveDevice(new WString(id), role.ordinal(), osd ? 1 : 0);
  }
}
