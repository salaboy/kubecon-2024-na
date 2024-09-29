package io.dapr.kubecon.examples.producer;

import io.dapr.testcontainers.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.junit.runner.Description;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.runners.model.Statement;

@TestConfiguration(proxyBeanMethods = false)
public class DaprTestContainersConfig {

   @Bean
   public Network getNetwork() {
      Network defaultDaprNetwork = new Network() {
        @Override
        public String getId() {
          return "dapr-network";
        }

        @Override
        public void close() {

        }

        @Override
        public Statement apply(Statement base, Description description) {
          return null;
        }
      };

      List<com.github.dockerjava.api.model.Network> networks = DockerClientFactory.instance().client().listNetworksCmd().withNameFilter("dapr-network").exec();
      if (networks.isEmpty()) {
        Network.builder()
                .createNetworkCmdModifier(cmd -> cmd.withName("dapr-network"))
                .build().getId();
        return defaultDaprNetwork;
      } else {
        return defaultDaprNetwork;
      }
   }


   @Bean
   public RabbitMQContainer rabbitMQContainer(Network daprNetwork){
      return new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.7.25-management-alpine"))
              .withExposedPorts(5672)
              .withNetworkAliases("rabbitmq")
              .withNetwork(daprNetwork);

   }


    @Bean
    @Scope("singleton")
    @ServiceConnection("otel/opentelemetry-collector-contrib")
    GenericContainer<?> lgtmContainer(Network daprNetwork) {
      return new GenericContainer<>("docker.io/grafana/otel-lgtm:0.7.1")
              .withExposedPorts(3000, 4317, 4318)
              .withEnv("OTEL_METRIC_EXPORT_INTERVAL", "100")
              .waitingFor(Wait.forLogMessage(".*The OpenTelemetry collector and the Grafana LGTM stack are up and running.*\\s", 1))
              .withStartupTimeout(Duration.ofMinutes(2))
              .withNetwork(daprNetwork)
              .withNetworkAliases("otel");
    }

   @Bean
   @ServiceConnection
   public DaprContainer daprContainer(Network daprNetwork, RabbitMQContainer rabbitMQContainer){

     Map<String, String> rabbitMqProperties = new HashMap<>();
     rabbitMqProperties.put("connectionString", "amqp://guest:guest@rabbitmq:5672");
     rabbitMqProperties.put("user", "guest");
     rabbitMqProperties.put("password", "guest");


     return new DaprContainer("daprio/daprd:1.14.1")
             .withAppName("producer-app-dapr")
             .withNetwork(daprNetwork)
             .withComponent(new Component("pubsub", "pubsub.rabbitmq", "v1", rabbitMqProperties))
//             .withConfiguration(new Configuration("my-config", new TracingConfigParameters("1.0", true,
//                     "76e76f0f90a2437895dc234ccf381f13.apm.us-central1.gcp.cloud.es.io:443", true, "http")))
//             .withConfiguration(new Configuration("my-config", new TracingConfigParameters("1.0", true,
//                     "otel:4318", false, "http")))
             .withDaprLogLevel(DaprLogLevel.DEBUG)
             .withLogConsumer(outputFrame -> System.out.println(outputFrame.getUtf8String()))
             .withAppPort(8080)
             .withAppChannelAddress("host.testcontainers.internal")
             .dependsOn(rabbitMQContainer);
   }


}
