package io.dapr.pubsub.examples.producer;

public class Payload {
  private String content;

  public Payload() {
  }

  public Payload(String content) {
    this.content = content;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }
}
