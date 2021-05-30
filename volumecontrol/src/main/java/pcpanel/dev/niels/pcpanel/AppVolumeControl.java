package pcpanel.dev.niels.pcpanel;

import dev.niels.pcpanel.plugins.AnalogAction;
import dev.niels.pcpanel.plugins.config.ConfigElementType;
import dev.niels.pcpanel.plugins.config.ConfigPageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Component
public class AppVolumeControl implements AnalogAction {
  @Override public String getName() {
    return "App volume";
  }

  @Override public List<ConfigPageBuilder.ConfigElement> getConfigElements() {
    return new ConfigPageBuilder()
      .addElement("application", "Application", ConfigElementType.filePicker)
      .addElement("a", "AAA", ConfigElementType.textField)
      .addElement("b", "BBB", ConfigElementType.textArea)
      .addElement("c", "CCC", ConfigElementType.slider)
      .getConfigElements();
  }
}
