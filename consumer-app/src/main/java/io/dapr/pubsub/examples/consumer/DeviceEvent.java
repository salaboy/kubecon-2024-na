package io.dapr.pubsub.examples.consumer;

public class DeviceEvent {

  private String id;
  private String device;
  private Payload payload;

  public DeviceEvent() {
  }

  public DeviceEvent(String id, String device, Payload payload) {
    this.id = id;
    this.device = device;
    this.payload = payload;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDevice() {
    return device;
  }

  public void setDevice(String device) {
    this.device = device;
  }

  public Payload getPayload() {
    return payload;
  }

  public void setPayload(Payload payload) {
    this.payload = payload;
  }
}
