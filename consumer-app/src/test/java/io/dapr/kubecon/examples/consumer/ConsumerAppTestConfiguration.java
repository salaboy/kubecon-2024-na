package io.dapr.kubecon.examples.consumer;

import io.dapr.client.DaprClient;
import io.dapr.spring.boot.autoconfigure.pubsub.DaprPubSubProperties;
import io.dapr.spring.messaging.DaprMessagingTemplate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({DaprPubSubProperties.class})
public class ConsumerAppTestConfiguration {
  @Bean
  public DaprMessagingTemplate<DeviceEvent> messagingTemplate(DaprClient daprClient,
                                                             DaprPubSubProperties daprPubSubProperties) {
    boolean observationEnabled = daprPubSubProperties.isObservationEnabled();
    String pubsubName = daprPubSubProperties.getName();
    return new DaprMessagingTemplate<>(daprClient, pubsubName, observationEnabled);
  }
}
