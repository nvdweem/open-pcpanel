package pcpanel.dev.niels.pcpanel;

import dev.niels.pcpanel.plugins.AnalogAction;
import dev.niels.pcpanel.plugins.Control;
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

  @Override public void triggerAction(Control control, FocusVolumeConfig config, int sliderPos) {
    var pos = Util.translateAnalog(sliderPos, config.isLogScaling(), config.getTrimMin(), config.getTrimMax());
    vcService.setFgVolume(pos, config.isOsd());
  }
}
