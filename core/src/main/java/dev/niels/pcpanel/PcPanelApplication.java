package dev.niels.pcpanel;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PcPanelApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(PcPanelApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }
}
