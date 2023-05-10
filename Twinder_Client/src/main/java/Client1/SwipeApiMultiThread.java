package Client1;

import io.swagger.client.*;
import io.swagger.client.model.*;
import io.swagger.client.api.SwipeApi;
import java.security.SecureRandom;
import java.util.concurrent.CountDownLatch;

public class SwipeApiMultiThread extends Thread {
  final static private int REQUESTNUM = 500_000;
  final static private int TOTALTHREADS = 80;
  private int REQPERTHREAD = REQUESTNUM/TOTALTHREADS;
  private int threadNumber;
  private static int successful = 0;
  private static int unsuccessful = 0;
  CountDownLatch completed;
  private static final SecureRandom random = new SecureRandom();
  ApiClient apiClient;
  SwipeApi apiInstance;

  public SwipeApiMultiThread(int threadNum, CountDownLatch completed) {
    this.threadNumber = threadNum;
    this.completed = completed;
    this.apiClient = new ApiClient();
//    apiClient.setBasePath("http://localhost:8080/cs6650_project1_war_exploded");
    apiClient.setBasePath("http://18.237.98.181:8080/cs6650-project1_war");
    this.apiInstance = new SwipeApi(apiClient);
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
    System.out.println("thread " + threadNumber + " is starting");
    int numberOfSuccessfulRequests = 0;
    int numberOfFailedRequests = 0;

    for (int i=1;i<=REQPERTHREAD;i++){
      Integer rdm = randomNum(256);
      String randomcomment = randomComment(rdm);
      SwipeDetails body = new SwipeDetails();
      body.setSwiper(randomNum(5000).toString());
      body.setSwipee(randomNum(1000000).toString());
      body.setComment(randomcomment);
      String leftorright = randomLeftRight(randomNum(1000));
      boolean isRequestSuccessful = makeRequest(body, leftorright);
      if (isRequestSuccessful) {
        numberOfSuccessfulRequests++;
      } else{
        numberOfFailedRequests++;
      }
    }
    System.out.println("thread " + threadNumber + " is completed");
    System.out.println(numberOfSuccessfulRequests + " " + numberOfFailedRequests);


    SwipeApiMultiThread.successful += numberOfSuccessfulRequests;
    SwipeApiMultiThread.unsuccessful += numberOfFailedRequests;
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

  public static void main(String[] args) throws InterruptedException {

    int start = (int) System.currentTimeMillis();
    CountDownLatch completed = new CountDownLatch(TOTALTHREADS);
    for (int i = 0; i <TOTALTHREADS ; i++) {
      SwipeApiMultiThread thread1 = new SwipeApiMultiThread(i, completed);
      thread1.start();
    };

    completed.await();
    int end = (int) System.currentTimeMillis();
    Double elapsedTime = Double.valueOf(end - start);
    Double throughput = REQUESTNUM / elapsedTime;
    System.out.println("Time elapsed: " + elapsedTime);
    System.out.println("Successful requests: " + successful);
    System.out.println("Unsuccessful requests: " + unsuccessful);
    System.out.println("Throughput: " + throughput);

  }
}