package dev.niels.pcpanel.plugins.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DropDownOption {
  private String value;
  private String display;
}
