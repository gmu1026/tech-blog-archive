package net.cproduction.techblogarchive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TechBlogArchiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(TechBlogArchiveApplication.class, args);
    }

}
