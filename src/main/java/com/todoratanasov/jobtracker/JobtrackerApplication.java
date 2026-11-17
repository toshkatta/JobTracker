package com.todoratanasov.jobtracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class JobtrackerApplication {

  public static void main(String[] args) {
    SpringApplication.run(JobtrackerApplication.class, args);
  }

}
