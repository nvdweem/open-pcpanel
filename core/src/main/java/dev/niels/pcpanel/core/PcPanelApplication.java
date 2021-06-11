package dev.niels.pcpanel.core;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.awt.Color;

@EnableScheduling
@SpringBootApplication(scanBasePackages = {"dev.niels", "pcpanel"})
public class PcPanelApplication {
  public static void main(String[] args) {
    new SpringApplicationBuilder(PcPanelApplication.class)
      .headless(false)
      .run(args);
  }

  @Bean
  public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
    return builder -> builder
      .serializerByType(Color.class, new JsonColor.ColorSerializer())
      .deserializerByType(Color.class, new JsonColor.ColorDeserializer());
  }
}
