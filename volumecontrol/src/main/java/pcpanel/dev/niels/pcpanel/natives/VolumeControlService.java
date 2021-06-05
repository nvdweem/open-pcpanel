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
  @Getter private Map<String, String> devices = new HashMap<>();
  @Getter private Map<Long, String> sessions = new HashMap<>();

  @PostConstruct
  public void construct() {
    WindowsSndLibrary.INSTANCE.init(
      (name, id) -> devices.put(id.toString(), name.toString()),
      name -> devices.remove(name.toString()),
      (pid, process) -> sessions.put(pid, process.toString()),
      pid -> sessions.remove(pid)
    );
  }

  public void setFgVolume(int volume, boolean osd) {
    WindowsSndLibrary.INSTANCE.setFgProcessVolume(volume, osd);
  }

  public boolean toggleDeviceMute(String device, boolean osd) {
    return WindowsSndLibrary.INSTANCE.toggleDeviceMute(new WString(device), osd);
  }

}
