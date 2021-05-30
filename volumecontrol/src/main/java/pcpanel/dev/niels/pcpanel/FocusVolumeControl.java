package pcpanel.dev.niels.pcpanel;

import dev.niels.pcpanel.plugins.AnalogAction;
import dev.niels.pcpanel.plugins.config.ConfigElementType;
import dev.niels.pcpanel.plugins.config.ConfigPageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Component
public class FocusVolumeControl implements AnalogAction {
  @Override public String getName() {
    return "Focus volume";
  }

  @Override public List<ConfigPageBuilder.ConfigElement> getConfigElements() {
    return new ConfigPageBuilder()
      .addElement("label", "Nothing to configure, changing this control will change the focused volume.", ConfigElementType.label)
      .getConfigElements();
  }
}
