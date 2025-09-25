package com.openstep.balllinkbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class BallLinkBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(BallLinkBeApplication.class, args);
    }

}
