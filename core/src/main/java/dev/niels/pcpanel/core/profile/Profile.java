package dev.niels.pcpanel.core.profile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.niels.pcpanel.core.SpringContext;
import dev.niels.pcpanel.core.device.light.LightConfig;
import dev.niels.pcpanel.core.device.light.StaticLightConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Transient;
import java.awt.Color;
import java.util.function.Supplier;

@Data
@Slf4j
@Entity
public class Profile {
  @Id
  @GeneratedValue
  private Long id;

  @Column
  private String device;

  @Column
  private String name;

  @Lob
  @Column
  @JsonIgnore
  private String lights;

  @Lob
  @Column
  @JsonIgnore
  private String actions;

  @Transient private LightConfig lightConfig;
  @Transient private Actions actionsConfig;

  public Profile init() {
    return init(SpringContext.getBean(ObjectMapper.class));
  }

  public Profile init(ObjectMapper mapper) {
    lightConfig = tryParse(mapper, lights, LightConfig.class, () -> new StaticLightConfig().setColor(Color.black));
    actionsConfig = tryParse(mapper, actions, Actions.class, () -> null);
    return this;
  }

  public Profile prepareForSave(ObjectMapper objectMapper) {
    try {
      lights = objectMapper.writeValueAsString(lightConfig);
      actions = objectMapper.writeValueAsString(actionsConfig);
    } catch (JsonProcessingException e) {
      log.error("Unable to prepare {} for saving", this);
    }
    return this;
  }

  private <T> T tryParse(ObjectMapper mapper, String input, Class<T> clazz, Supplier<T> def) {
    if (StringUtils.hasText(input)) {
      try {
        return mapper.readValue(input, clazz);
      } catch (Exception e) {
        log.warn("Unable to read light config: {}", input);
      }
    }
    return null;
  }
}
