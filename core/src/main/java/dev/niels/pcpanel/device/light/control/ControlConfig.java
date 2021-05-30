package dev.niels.pcpanel.device.light.control;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dev.niels.pcpanel.helper.ByteArrayBuilder;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(LogoBreathConfig.class),
  @JsonSubTypes.Type(LogoRainbowConfig.class),
  @JsonSubTypes.Type(GradientConfig.class),
  @JsonSubTypes.Type(StaticConfig.class),
  @JsonSubTypes.Type(EmptyConfig.class),
  @JsonSubTypes.Type(VolumeGradientConfig.class)
})
public abstract class ControlConfig {
  private static final int COMMAND_LENGTH = 7;

  public void appendToBuilder(ByteArrayBuilder builder) {
    builder.mark();
    doAppend(builder);
    builder.pad(COMMAND_LENGTH);
  }

  protected abstract void doAppend(ByteArrayBuilder builder);
}
