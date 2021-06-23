package dev.niels.pcpanel.plugins.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import one.util.streamex.StreamEx;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DropDownOption {
  private String value;
  private String display;

  public static List<DropDownOption> fromList(Object value) {
    if (value instanceof Collection) {
      return StreamEx.of((Collection<?>) value).map(DropDownOption::from).nonNull().toList();
    }
    return null;
  }

  public static DropDownOption from(Object value) {
    if (value instanceof DropDownOption) {
      return (DropDownOption) value;
    }
    if (value instanceof Map) {
      @SuppressWarnings("unchecked")
      var map = ((Map<String, String>) value);
      return new DropDownOption(map.get("value"), map.get("display"));
    }
    return null;
  }
}
