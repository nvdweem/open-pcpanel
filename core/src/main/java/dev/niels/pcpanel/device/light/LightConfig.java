package dev.niels.pcpanel.device.light;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(CustomLightConfig.class),
  @JsonSubTypes.Type(BreathLightConfig.class),
  @JsonSubTypes.Type(RainbowLightConfig.class),
  @JsonSubTypes.Type(StaticLightConfig.class),
  @JsonSubTypes.Type(WaveLightConfig.class)
})
public abstract class LightConfig {
  public abstract byte[][] toCommand();
}
