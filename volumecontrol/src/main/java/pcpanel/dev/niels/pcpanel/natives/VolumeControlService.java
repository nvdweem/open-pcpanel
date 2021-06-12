package pcpanel.dev.niels.pcpanel.natives;

import com.sun.jna.WString;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class VolumeControlService {
  @Getter private static Map<String, VolumeDevice> devices = new HashMap<>();
  @Getter private static Map<Long, VolumeSession> sessions = new HashMap<>();

  @PostConstruct
  public void construct() {
    VolumeControlLib.INSTANCE.initialize(
      VolumeControlService::deviceChanged, name -> devices.remove(name.toString()),
      VolumeControlService::sessionChanged, pid -> sessions.remove(pid),
      d -> log.debug("{}", d), i -> log.info("{}", i)
    );
  }

  private static void sessionChanged(long pid, WString process, WString icon, int volume, boolean muted) {
    var session = sessions.computeIfAbsent(pid, VolumeSession::new);
    session.setProcess(process.toString());
    session.setIcon(icon.toString());
    session.setVolume(volume);
    session.setMuted(muted);
  }

  private static void deviceChanged(WString name, WString id, int volume, boolean muted) {
    var device = devices.computeIfAbsent(id.toString(), VolumeDevice::new);
    device.setName(name.toString());
    device.setVolume(volume);
    device.setMuted(muted);
  }

  public void setFgVolume(int volume, boolean osd) {
    VolumeControlLib.INSTANCE.setFgProcessVolume(volume, osd);
  }

  public boolean toggleDeviceMute(String device, boolean osd) {
    return VolumeControlLib.INSTANCE.toggleDeviceMute(new WString(device), osd);
  }

}
