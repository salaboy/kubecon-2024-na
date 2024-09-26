package io.dapr.kubecon.examples.producer;

import io.dapr.spring.boot.autoconfigure.client.DaprConnectionDetails;
import io.dapr.spring.messaging.DaprMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RestController
public class DeviceRestController {

  @Autowired
  private DaprMessagingTemplate<DeviceEvent> messagingTemplate;

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private DaprConnectionDetails daprConnectionDetails;

  @Value("${DAPR_API_TOKEN:}")
  private String daprApiToken;

  private List<DeviceEvent> events = new ArrayList<>();

  @PostMapping("/device/events")
  public void iotEvent(@RequestBody DeviceEvent event){
      events.add(event);
      messagingTemplate.send("topic", event);
      System.out.println("+++ PRODUCING EVENT: " + event);
  }

  @GetMapping("/device/events")
  public Iterable<DeviceEvent> getAll(){
    return events;
  }


  @PostMapping("/device/info")
  public InfoRequest requestInfo(@RequestBody InfoRequest request){
    HttpHeaders headers = new HttpHeaders();
    headers.add("dapr-api-token", daprApiToken);
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<InfoRequest> postRequest = new HttpEntity<>(request, headers);

    String url = daprConnectionDetails.httpEndpoint()+"/v1.0/invoke/consumer-app-dapr/method/info";
    System.out.println("+++ INVOKING SERVICE: " + url + " - Using Token: "+ daprApiToken);
    return restTemplate.postForObject(url, postRequest, InfoRequest.class);
  }


}

