package io.dapr.pubsub.examples.producer;

import io.dapr.spring.messaging.DaprMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class DeviceRestController {

  @Autowired
  private DaprMessagingTemplate<DeviceEvent> messagingTemplate;

  private List<DeviceEvent> events = new ArrayList<>();

  @PostMapping("/device/events")
  public void iotEvent(@RequestBody DeviceEvent event){
    events.add(event);
    messagingTemplate.send("topic", event);
  }

  @GetMapping("/device/events")
  public Iterable<DeviceEvent> getAll(){
    return events;
  }


}

