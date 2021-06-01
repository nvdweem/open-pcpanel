package pcpanel.dev.niels.pcpanel.natives;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class VolumeControlService {
  private List<String> dIds = new ArrayList<>();
  private List<String> sIds = new ArrayList<>();

  @PostConstruct
  public void construct() {
    System.out.println("Test!");

    WindowsSndLibrary.INSTANCE.init(
      (a, b) -> {
        log.error("Device added!!! {}, {}", a, b);
        dIds.add(b.toString());
      },
      a -> log.error("Device removed: {}", a),
      (a, b) -> {
        log.error("    Session added!!! {}, {}", a, b);
        sIds.add(b.toString());
      },
      a -> log.error("    Session removed: {}", a)
    );

  }

  public void setFgVolume(int volume, boolean osd) {
    WindowsSndLibrary.INSTANCE.setFgProcessVolume(volume, osd);
  }
}
