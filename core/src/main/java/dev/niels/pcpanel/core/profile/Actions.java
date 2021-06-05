package dev.niels.pcpanel.core.profile;

import dev.niels.pcpanel.core.device.Device;
import dev.niels.pcpanel.plugins.AnalogAction;
import dev.niels.pcpanel.plugins.Control;
import dev.niels.pcpanel.plugins.KnobAction;
import dev.niels.pcpanel.plugins.config.ActionConfig;
import lombok.Data;
import one.util.streamex.IntStreamEx;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
public class Actions {
  private final List<ActionConfig> knobActions = new ArrayList<>();
  private final List<ActionConfig> analogActions = new ArrayList<>();

  public static Actions forDevice(Device device) {
    var result = new Actions();
    result.knobActions.addAll(IntStreamEx.range(device.getButtonCount()).mapToObj(i -> new EmptyAction()).toList());
    result.analogActions.addAll(IntStreamEx.range(device.getAnalogCount()).mapToObj(i -> new EmptyAction()).toList());
    return result;
  }

  public void setKnobAction(int idx, ActionConfig action) {
    knobActions.set(idx, action);
  }

  public void setAnalogAction(int idx, ActionConfig action) {
    analogActions.set(idx, action);
  }

  @Component
  public static class EmptyAction implements KnobAction<EmptyAction>, AnalogAction<EmptyAction>, ActionConfig {
    @Override public String getName() {
      return "Empty action";
    }

    @Override public Class<EmptyAction> getConfigurationClass() {
      return EmptyAction.class;
    }

    @Override public Class<EmptyAction> getActionClass() {
      return EmptyAction.class;
    }

    @Override public void triggerAction(Control c, EmptyAction config, int sliderPos) {
    }

    @Override public void buttonDown(Control control, EmptyAction config) {
    }
  }
}
