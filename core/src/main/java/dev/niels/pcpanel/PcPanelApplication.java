package dev.niels.pcpanel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PcPanelApplication {
    public static void main(String[] args) {
        SpringApplication.run(PcPanelApplication.class, args);
    }
}
