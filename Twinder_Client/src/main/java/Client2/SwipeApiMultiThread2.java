package Client2;
import com.google.gson.Gson;
import com.opencsv.CSVWriter;
import io.swagger.client.*;
import io.swagger.client.api.MatchesApi;
import io.swagger.client.api.StatsApi;
import io.swagger.client.auth.*;
import io.swagger.client.model.*;
import io.swagger.client.api.SwipeApi;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class SwipeApiMultiThread2 extends Thread {
  final static private int REQUESTNUM = 1000;
  final static private int TOTALTHREADS = 16;
  private int REQPERTHREAD = REQUESTNUM/TOTALTHREADS;
  private int threadNumber;
  private static int successful = 0;
  private static int unsuccessful = 0;
  private static ArrayList<RequestData> requestData = new ArrayList<>();
  CountDownLatch completed;
  private static final SecureRandom random = new SecureRandom();
  ApiClient apiClient;
  SwipeApi apiInstance;
  MatchesApi matchesApiInstance;
  StatsApi statsApiInstance;

  public SwipeApiMultiThread2(int threadNum, CountDownLatch completed) {
    this.threadNumber = threadNum;
    this.completed = completed;
    this.apiClient = new ApiClient();
    apiClient.setBasePath("http://localhost:8080/Twinder2_server_war_exploded/");
//    apiClient.setBasePath("http://twinderserver-1035510586.us-west-2.elb.amazonaws.com:8080/Twinder2_server_war");
//    apiClient.setBasePath("http://34.211.231.25:8080/Twinder2_server_war");
    this.apiInstance = new SwipeApi(apiClient);
    this.matchesApiInstance = new MatchesApi(apiClient);
    this.statsApiInstance = new StatsApi(apiClient);
  }

  private boolean isSuccessfulRequest(int statusCode) {
    return statusCode == 200 || statusCode == 201;
  }

  private boolean makeRequest(SwipeDetails body, String leftOrRight) {
    try {
      ApiResponse<Void> res = apiInstance.swipeWithHttpInfo(body, leftOrRight);
      boolean wasRequestSuccessful = isSuccessfulRequest(res.getStatusCode());
      if (wasRequestSuccessful) return true;

      for (int n = 0; n < 5; n++) {
        if (makeRequest(body, leftOrRight)) return true;
      }
    } catch (ApiException e) {
      return false;
    }
    return false;
  }

  public void run(){
    int numberOfSuccessfulRequests = 0;
    int numberOfFailedRequests = 0;
    ArrayList<RequestData> throughputs = new ArrayList<>();

    for (int i=1;i<=REQPERTHREAD;i++){
      int responseCode;
      String reqType = "POST";
      int startRequest = ((int) System.currentTimeMillis());
      Integer rdm = randomNum(256);
      String randomcomment = randomComment(rdm);
      SwipeDetails body = new SwipeDetails();
      body.setSwiper(randomNum(5000).toString());
      body.setSwipee(randomNum(5000).toString());
      body.setComment(randomcomment);
      String leftorright = randomLeftRight(randomNum(1000));
      boolean isRequestSuccessful = makeRequest(body, leftorright);
      if (isRequestSuccessful) {
        numberOfSuccessfulRequests++;
        responseCode = 200;
      } else{
        numberOfFailedRequests++;
        responseCode = 400;
      }
      int endRequest = ((int) System.currentTimeMillis());
      int latency = endRequest - startRequest;
      RequestData newReq = new RequestData(startRequest, reqType, latency, responseCode);
      throughputs.add(newReq);

    }



    SwipeApiMultiThread2.successful += numberOfSuccessfulRequests;
    SwipeApiMultiThread2.unsuccessful += numberOfFailedRequests;
    SwipeApiMultiThread2.requestData.addAll(throughputs);
    completed.countDown();
  }

  public static Integer randomNum(Integer range){
    int num = random.nextInt(range);
    return num + 1;
  }

  public static String randomLeftRight(Integer randomNum){
    if(randomNum%2==0){
      return "left";
    }else{
      return "right";
    }
  }

  public static String randomComment(Integer length){
    final char[] validchars = ("0123456789abcdefghijklmnopqrstuvwxyz-_ABCDEFGHIJKLMNOPQRSTUV"
        + "WXYZ").toCharArray();
    StringBuilder comment = new StringBuilder();
    for (int i = 0; i < length; i++) {
      comment.append(validchars[random.nextInt(validchars.length)]);
    }
    return comment.toString();
  }

  public static void writeToCSV(String filePath){
    File file = new File(filePath);
    try {
      FileWriter outputfile = new FileWriter(file);

      CSVWriter writer = new CSVWriter(outputfile);

      String[] header = { "StartTime", "RequestType", "Latency", "ResponseCode" };
      writer.writeNext(header);

      for(int i = 0; i<requestData.size();i++){
        RequestData rec = requestData.get(i);
        String[] record = {String.valueOf(rec.getStartTime()), rec.getRequestType(),
            String.valueOf(rec.getLatency()), String.valueOf(rec.getResponseCode())};
        writer.writeNext(record);
      }
      writer.close();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws InterruptedException {

    int start = (int) System.currentTimeMillis();
    CountDownLatch completed = new CountDownLatch(TOTALTHREADS);
    GetThread getthread = new GetThread(completed);
    getthread.start();
    for (int i = 0; i <TOTALTHREADS ; i++) {
      SwipeApiMultiThread2 thread1 = new SwipeApiMultiThread2(i, completed);
      thread1.start();
    }
    completed.await();
    getthread.running = false;
    int end = (int) System.currentTimeMillis();
    Double elapsedTime = Double.valueOf(end - start)/1000;
    Double throughput = REQUESTNUM / elapsedTime;
    System.out.println("POST THREAD");
    System.out.println("--------------------------");
    System.out.println("Time elapsed: " + elapsedTime);
    System.out.println("Successful requests: " + successful);
    System.out.println("Unsuccessful requests: " + unsuccessful);
    System.out.println("Throughput: " + throughput);

    Collections.sort(requestData);
    DescriptiveStatistics stats = new DescriptiveStatistics();
    ArrayList<Integer> latencies = new ArrayList<>();
    for (RequestData request:requestData){
      stats.addValue(request.getLatency());
      latencies.add(request.getLatency());
    }

    System.out.println("Mean: " + stats.getMean() + "ms");
    System.out.println("Median: " + stats.getPercentile(50) + "ms");
    System.out.println("P99: " + stats.getPercentile(99) + "ms");
    System.out.println("Min: " + stats.getMin() + "ms");
    System.out.println("Max: " + stats.getMax() + "ms");
    System.out.println("--------------------------");

    System.out.println("GET THREAD");
    System.out.println("--------------------------");
    DescriptiveStatistics getstats = new DescriptiveStatistics();
    for (Long latency: getthread.latencies){
      getstats.addValue(latency);
    }
    System.out.println("Mean: " + getstats.getMean() + "ms");
    System.out.println("Min: " + getstats.getMin() + "ms");
    System.out.println("Max: " + getstats.getMax() + "ms");
    System.out.println("--------------------------");
    };

//    writeToCSV("/Users/stefanisindarto/Northeastern/cs6650/cs6650-project1-client/result.csv");
  }
