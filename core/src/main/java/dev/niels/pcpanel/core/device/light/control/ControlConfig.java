package dev.niels.pcpanel.core.device.light.control;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dev.niels.pcpanel.core.helper.ByteArrayBuilder;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = LogoBreathConfig.class, name = "breath"),
  @JsonSubTypes.Type(value = LogoRainbowConfig.class, name = "rainbow"),
  @JsonSubTypes.Type(value = GradientConfig.class, name = "gradient"),
  @JsonSubTypes.Type(value = StaticConfig.class, name = "static"),
  @JsonSubTypes.Type(value = VolumeGradientConfig.class, name = "volumeGradient")
})
public abstract class ControlConfig {
  private static final int COMMAND_LENGTH = 7;

  public void appendToBuilder(ByteArrayBuilder builder) {
    builder.mark();
    doAppend(builder);
    builder.pad(COMMAND_LENGTH);
  }

  protected abstract void doAppend(ByteArrayBuilder builder);

  public abstract ControlConfig copy();
}
