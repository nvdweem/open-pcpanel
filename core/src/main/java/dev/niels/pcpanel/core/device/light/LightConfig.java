package dev.niels.pcpanel.core.device.light;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = CustomLightConfig.class, name = "custom"),
  @JsonSubTypes.Type(value = BreathLightConfig.class, name = "breath"),
  @JsonSubTypes.Type(value = RainbowLightConfig.class, name = "rainbow"),
  @JsonSubTypes.Type(value = StaticLightConfig.class, name = "static"),
  @JsonSubTypes.Type(value = WaveLightConfig.class, name = "wave")
})
public abstract class LightConfig {
  public abstract byte[][] toCommand();

  public abstract LightConfig copy();
}
