package pcpanel.dev.niels.pcpanel;

import dev.niels.pcpanel.plugins.AnalogAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import pcpanel.dev.niels.pcpanel.natives.VolumeControlService;

@Component
@RestController
@RequiredArgsConstructor
public class FocusVolumeControl implements AnalogAction<FocusVolumeConfig> {
  private final VolumeControlService vcService;

  @Override public String getName() {
    return "Focus volume";
  }

  @Override public Class<FocusVolumeConfig> getConfigurationClass() {
    return FocusVolumeConfig.class;
  }

  @Override public void triggerAction(FocusVolumeConfig config, int sliderPos) {
    vcService.setFgVolume(Math.round((sliderPos / 255f) * 100), true);
  }
}
