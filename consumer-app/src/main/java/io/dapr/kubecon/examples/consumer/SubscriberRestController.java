package io.dapr.kubecon.examples.consumer;

import io.dapr.Topic;
import io.dapr.client.domain.CloudEvent;
import okhttp3.Response;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class SubscriberRestController {

  private Boolean syncWorking = true;
  private Boolean aSyncWorking = true;

  private List<CloudEvent> events = new ArrayList<>();
  private AtomicInteger syncCounter = new AtomicInteger(0);

  private final SimpMessagingTemplate simpMessagingTemplate;

  public SubscriberRestController(SimpMessagingTemplate simpMessagingTemplate) {
    this.simpMessagingTemplate = simpMessagingTemplate;
  }

  private void emitWSEvent(Object event) {
    System.out.println("Emitting Event via WS: " + event.toString());
    simpMessagingTemplate.convertAndSend("/topic/events",
            event);
  }

  @PostMapping("subscribe")
  @Topic(pubsubName = "pubsub", name = "topic")
  public ResponseEntity<Void> subscribe(@RequestBody CloudEvent<DeviceEvent> cloudEvent){
    if(!aSyncWorking){
      emitWSEvent(new WSEvent("async-error", 0));
      return ResponseEntity.internalServerError().build();
    }
    System.out.println("CONSUME +++++ " + cloudEvent);
    System.out.println("DATA +++++ " + cloudEvent.getData());
    events.add(cloudEvent);
    emitWSEvent(new WSEvent("async", events.size()));
    return ResponseEntity.ok().build();
  }

  @GetMapping("events")
  public List<CloudEvent> getAllEvents() {
    return events;
  }

  @GetMapping("eventsCount")
  public Integer getEventsCount() {
    return events.size();
  }

  @GetMapping("syncCount")
  public Integer getSyncCount() {
    return syncCounter.get();
  }

  @PostMapping("info")
  public ResponseEntity<InfoRequest> info(@RequestBody InfoRequest request){
    if(!syncWorking){
      emitWSEvent(new WSEvent("sync-error", 0));
      return ResponseEntity.internalServerError().build();
    }
    request.setContent(request.getContent()+"-validated");
    System.out.println("INFO REQUEST: " + request);
    int count = syncCounter.incrementAndGet();
    emitWSEvent(new WSEvent("sync", count));
    return ResponseEntity.ok(request);
  }

  @PostMapping("breakfixsync")
  public void fixBreakSync(){
    if(syncWorking){
      syncWorking = false;
    }else{
      syncWorking = true;
    }
    System.out.println("Sync Working?? " + syncWorking);
  }

  @PostMapping("breakfixasync")
  public void fixBreakAsync(){
    if(aSyncWorking){
      aSyncWorking = false;
    }else{
      aSyncWorking = true;
    }
    System.out.println("ASync Working?? " + aSyncWorking);
  }

  record WSEvent(String type, Integer count){}

}

