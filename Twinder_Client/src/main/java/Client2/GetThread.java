package Client2;

import io.swagger.client.*;
import io.swagger.client.api.MatchesApi;
import io.swagger.client.api.StatsApi;
import io.swagger.client.model.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class GetThread extends Thread {
  ArrayList<Long> latencies = new ArrayList<>();
  CountDownLatch completed;

  private final static String Base_Path = "http://localhost:8080/Twinder2_server_war_exploded/";
  public volatile boolean running = true;

  public GetThread(CountDownLatch completed) {
    this.completed = completed;
  }

  public void run(){
    ApiClient apiClient = new ApiClient();
    apiClient.setBasePath(Base_Path);
    MatchesApi matchesApiInstance = new MatchesApi(apiClient);
    StatsApi statsApiInstance = new StatsApi(apiClient);
    while(running) {
      try {
        for (int i = 1; i <= 5; i++) {
          long startTime = System.currentTimeMillis();
          String rdmSwiper = String.valueOf(ThreadLocalRandom.current().nextInt(1, 5000));
          ApiResponse<Matches> matchesApiResponse = matchesApiInstance.matchesWithHttpInfo(
              rdmSwiper);
          ApiResponse<MatchStats> matchStatsApiResponse = statsApiInstance.matchStatsWithHttpInfo(
              rdmSwiper);
          System.out.println("get req "+ i);
          long endTime = System.currentTimeMillis();
          long latency = endTime - startTime;
          System.out.println(latency);
          latencies.add(latency);
        }
        Thread.sleep(1000);
      } catch (ApiException | InterruptedException e) {
        e.printStackTrace();
        e.getCause().printStackTrace();
      }
    }
  }



}
