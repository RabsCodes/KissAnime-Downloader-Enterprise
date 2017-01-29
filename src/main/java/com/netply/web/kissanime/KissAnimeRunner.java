package com.netply.web.kissanime;

import com.netply.web.kissanime.data.Credentials;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.logging.Level;
import java.util.logging.Logger;

@SpringBootApplication(scanBasePackages = {
        "com.netply.web.kissanime"
})
public class KissAnimeRunner {
    public static void main(String[] args) {
        Credentials.USERNAME = System.getenv("KA_USER");
        Credentials.PASSWORD = System.getenv("KA_PASS");
        if (Credentials.USERNAME == null || Credentials.PASSWORD == null) {
            throw new RuntimeException("KA_USER or KA_PASS environment variable not set");
        }

        Logger.getGlobal().setLevel(Level.ALL);
        ConfigurableApplicationContext context = SpringApplication.run(KissAnimeRunner.class, args);
        context.getBean(AnimeThreadManager.class).startQueueThread();
    }
}
