package dev.niels.pcpanel.core;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = {"dev.niels", "pcpanel"})
public class PcPanelApplication {
  public static void main(String[] args) {
    new SpringApplicationBuilder(PcPanelApplication.class)
      .headless(false)
      .run(args);
  }
}
