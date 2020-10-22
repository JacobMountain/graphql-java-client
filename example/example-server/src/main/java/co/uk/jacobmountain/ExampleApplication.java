package co.uk.jacobmountain;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class ExampleApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(ExampleApplication.class)
                .run(args);
    }

}
