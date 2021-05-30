package dev.niels.pcpanel.core.profile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.niels.pcpanel.core.device.light.LightConfig;
import dev.niels.pcpanel.core.device.light.StaticLightConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Transient;
import java.awt.Color;

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

  @Transient private LightConfig lightConfig;

  public Profile init(ObjectMapper mapper) {
    try {
      lightConfig = mapper.readValue(lights, LightConfig.class);
    } catch (Exception e) {
      log.warn("Unable to read light config: {}", lights);
      lightConfig = new StaticLightConfig().setColor(Color.black);
    }
    return this;
  }

  public Profile prepareForSave(ObjectMapper objectMapper) {
    try {
      this.lights = objectMapper.writeValueAsString(lightConfig);
    } catch (JsonProcessingException e) {
      log.error("Unable to prepare {} for saving", this);
    }
    return this;
  }
}
