package io.dapr.kubecon.examples.consumer;

public class InfoRequest {
  private String id;
  private String content;

  public InfoRequest() {
  }

  public InfoRequest(String id, String content) {
    this.id = id;
    this.content = content;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  @Override
  public String toString() {
    return "InfoRequest{" +
            "id='" + id + '\'' +
            ", content='" + content + '\'' +
            '}';
  }
}
