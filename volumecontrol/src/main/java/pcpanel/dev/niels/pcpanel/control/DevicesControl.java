package pcpanel.dev.niels.pcpanel.control;

import dev.niels.pcpanel.plugins.config.DropDownOption;
import lombok.RequiredArgsConstructor;
import one.util.streamex.EntryStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pcpanel.dev.niels.pcpanel.natives.VolumeControlService;

import java.util.List;

@RequiredArgsConstructor
@RestController()
@RequestMapping("volumecontrol")
public class DevicesControl {
  private final VolumeControlService vcService;

  @GetMapping("devices")
  public List<DropDownOption> getDevices() {
    return EntryStream.of(vcService.getDevices()).mapKeyValue(DropDownOption::new).toList();
  }
}
