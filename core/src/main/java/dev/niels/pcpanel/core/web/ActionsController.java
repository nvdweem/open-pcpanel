package dev.niels.pcpanel.core.web;

import dev.niels.pcpanel.core.profile.Actions;
import dev.niels.pcpanel.plugins.Action;
import dev.niels.pcpanel.plugins.AnalogAction;
import dev.niels.pcpanel.plugins.KnobAction;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import one.util.streamex.StreamEx;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("actions")
public class ActionsController {
  private final List<AnalogAction<?>> analogActions;
  private final List<KnobAction<?>> knobActions;

  @GetMapping
  public AllActions getAllActions() {
    return new AllActions(getAnalogActions(), getKnobActions());
  }

  @GetMapping("analog")
  public List<NameWithElements> getAnalogActions() {
    return StreamEx.of(analogActions).removeBy(Object::getClass, Actions.EmptyAction.class).map(this::toNameWithElements).toList();
  }

  @GetMapping("knob")
  public List<NameWithElements> getKnobActions() {
    return StreamEx.of(knobActions).removeBy(Object::getClass, Actions.EmptyAction.class).map(this::toNameWithElements).toList();
  }

  @Data
  public static class AllActions {
    private final List<NameWithElements> analogActions;
    private final List<NameWithElements> knobActions;
  }

  private NameWithElements toNameWithElements(Action<?> action) {
    var elements = new ArrayList<Map<String, Object>>();
    ReflectionUtils.doWithFields(action.getConfigurationClass(), f -> {
      var as = f.getAnnotations();
      for (var a : as) {
        var map = AnnotationUtils.getAnnotationAttributes(a);
        if (map.containsKey("type")) {
          var editable = new HashMap<>(map);
          editable.put("name", f.getName());
          elements.add(editable);
        }
      }
    });

    return new NameWithElements(action.getName(), action.getImpl(), elements);
  }

  @Data
  public static class NameWithElements {
    private final String name;
    private final String impl;
    private final List<Map<String, Object>> elements;
  }
}
