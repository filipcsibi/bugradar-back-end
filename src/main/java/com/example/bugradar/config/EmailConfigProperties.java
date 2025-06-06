package com.example.bugradar.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties(prefix = "app")
public class EmailConfigProperties {

    private String name = "Bug Radar";
    private Support support = new Support();

    public void setName(String name) {
        this.name = name;
    }

    public void setSupport(Support support) {
        this.support = support;
    }

    @Getter
    public static class Support {
        private String email = "support@bugradar.com";

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
