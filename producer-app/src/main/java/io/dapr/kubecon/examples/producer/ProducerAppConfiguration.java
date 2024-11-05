package io.dapr.kubecon.examples.producer;

import io.dapr.client.DaprClient;
import io.dapr.spring.boot.autoconfigure.pubsub.DaprPubSubProperties;
import io.dapr.spring.messaging.DaprMessagingTemplate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties({DaprPubSubProperties.class})
public class ProducerAppConfiguration {

  @Bean
  public DaprMessagingTemplate<DeviceEvent> messagingTemplate(DaprClient daprClient,
                                                              DaprPubSubProperties daprPubSubProperties) {
    boolean observationEnabled = daprPubSubProperties.isObservationEnabled();
    String pubsubName = daprPubSubProperties.getName();
    return new DaprMessagingTemplate<>(daprClient, pubsubName, observationEnabled);
  }


  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.build();
  }
}
