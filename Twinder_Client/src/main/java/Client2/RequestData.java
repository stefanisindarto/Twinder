package Client2;

public class RequestData implements Comparable<RequestData> {
  private int startTime;
  private String requestType;
  private int latency;
  private int responseCode;


  public RequestData(int startTime, String requestType, int latency, int responseCode) {
    this.startTime = startTime;
    this.requestType = requestType;
    this.latency = latency;
    this.responseCode = responseCode;
  }

  public int getStartTime() {
    return startTime;
  }

  public void setStartTime(int startTime) {
    this.startTime = startTime;
  }

  public String getRequestType() {
    return requestType;
  }

  public void setRequestType(String requestType) {
    this.requestType = requestType;
  }

  public int getLatency() {
    return latency;
  }

  public void setLatency(int latency) {
    this.latency = latency;
  }

  public int getResponseCode() {
    return responseCode;
  }

  public void setResponseCode(int responseCode) {
    this.responseCode = responseCode;
  }

  @Override
  public int compareTo(RequestData otherRequestData) {
  int comparison = otherRequestData.getStartTime();
    return this.getStartTime() - comparison;
  }
}
