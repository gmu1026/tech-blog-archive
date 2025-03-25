package net.cproduction.techblogarchive;

import org.springframework.boot.SpringApplication;

public class TestTechBlogArchiveApplication {

    public static void main(String[] args) {
        SpringApplication.from(TechBlogArchiveApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
