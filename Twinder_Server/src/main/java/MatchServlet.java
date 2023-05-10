import DAO.SwipeDataDao;
import Model.Swipe;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeoutException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MatchServlet extends HttpServlet {

  private static ConnectionFactory factory;
  private static RMQChannelPool channelPool;
  private static RMQChannelFactory channelFactory;
  private static Connection connection;
  private static Channel ch;
  private final static String EXCHANGE = "SwipeExchange";

  @Override
  public void init() throws ServletException {
    super.init();
    factory = new ConnectionFactory();
    factory.setHost("ec2-34-211-231-25.us-west-2.compute.amazonaws.com");
    factory.setPort(5672);
    factory.setUsername("guest");
    factory.setPassword("guest");
//    factory.setHost("localhost");
    connection = null;
    ch = null;
    try {
      connection = factory.newConnection();
      channelFactory = new RMQChannelFactory(connection);
      channelPool = new RMQChannelPool(1500, channelFactory);

    } catch (IOException e) {
      e.printStackTrace();
    } catch (TimeoutException e) {
      e.printStackTrace();
    }

  }

  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("application/json");
    String urlPath = req.getPathInfo();
    Gson gson = new Gson();
    HashMap<String, String> errorMessageMap = new LinkedHashMap<>();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      errorMessageMap.put("message", "missing parameter");
      String printJSON = gson.toJson(errorMessageMap);
      res.getWriter().print(printJSON);
      return;
    }
    String id = urlPath.substring(1, urlPath.length() - 1);
    Integer userId = Integer.parseInt(id);

    if (!isUrlValid(userId)) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      errorMessageMap.put("message", "invalid input");
      String printJSON = gson.toJson(errorMessageMap);
      res.getWriter().print(printJSON);
      return;
    }

    SwipeDataDao swipeDataDao = new SwipeDataDao();
    ArrayList<Integer> resultList = swipeDataDao.getMatches(userId);
    System.out.println("size: " + resultList.size());
    if (resultList.size() == 0){
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      errorMessageMap.put("message", "user not found");
      String printJSON = gson.toJson(errorMessageMap);
      res.getWriter().print(printJSON);
      return;
    }
    ArrayList<String> result = new ArrayList<String>();
    for(Integer i: resultList){
      result.add(String.valueOf(i));
    }
    HashMap<String, ArrayList<String>> resultmap = new HashMap<>();
    resultmap.put("matchlist", result);
    String printJSON = gson.toJson(resultmap);
    res.setStatus(HttpServletResponse.SC_OK);
    System.out.println("get matches for user " + userId);
    res.getWriter().print(printJSON);


  }


  private boolean isUrlValid(Integer id) {
    return id > 1 && id < 5000;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
//    http://localhost:8080/Twinder2_server_war_exploded/


  }
}
