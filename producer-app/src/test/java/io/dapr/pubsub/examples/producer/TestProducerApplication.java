package io.dapr.pubsub.examples.producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;


@SpringBootApplication
@AutoConfigureObservability
public class TestProducerApplication {

  public static void main(String[] args) {

    SpringApplication
            .from(ProducerApplication::main)
            .with(DaprTestContainersConfig.class)
            .run(args);
  }

}
