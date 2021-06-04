package pcpanel.dev.niels.pcpanel.analog;

import dev.niels.pcpanel.plugins.Action;
import dev.niels.pcpanel.plugins.AnalogAction;
import dev.niels.pcpanel.plugins.Control;
import dev.niels.pcpanel.plugins.config.ActionConfig;
import dev.niels.pcpanel.plugins.config.ConfigElement;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import pcpanel.dev.niels.pcpanel.Util;
import pcpanel.dev.niels.pcpanel.natives.VolumeControlService;

@Component
@RestController
@RequiredArgsConstructor
public class FocusVolumeControl implements AnalogAction<FocusVolumeControl.FocusVolumeConfig> {
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

  @Data
  public static class FocusVolumeConfig implements ActionConfig {
    @ConfigElement.Checkbox(label = "Show OSD") private boolean osd = false;
    @ConfigElement.Checkbox(label = "Logarithmic scaling") private boolean logScaling = false;
    @ConfigElement.Number(label = "Trim min") private Integer trimMin;
    @ConfigElement.Number(label = "Trim max") private Integer trimMax;

    @Override public Class<? extends Action<?>> getActionClass() {
      return FocusVolumeControl.class;
    }
  }
}
