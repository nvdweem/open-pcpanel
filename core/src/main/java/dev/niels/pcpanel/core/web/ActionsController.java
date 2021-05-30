package dev.niels.pcpanel.core.web;

import dev.niels.pcpanel.plugins.AnalogAction;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("actions")
public class ActionsController {
  private final List<AnalogAction> analogActions;

  @GetMapping
  public AllActions getAllActions() {
    return new AllActions(analogActions);
  }

  @GetMapping("analog")
  public List<AnalogAction> getAnalogActions() {
    return analogActions;
  }

  @Data
  public static class AllActions {
    private final List<AnalogAction> analogActions;
  }
}
