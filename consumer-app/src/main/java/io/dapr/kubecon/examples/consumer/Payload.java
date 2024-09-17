package io.dapr.kubecon.examples.consumer;

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

  @Override
  public String toString() {
    return "Payload{" +
            "content='" + content + '\'' +
            '}';
  }
}
