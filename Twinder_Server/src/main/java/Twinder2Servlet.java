import DAO.SwipeDataDao;
import Model.Swipe;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import com.google.gson.Gson;


public class Twinder2Servlet extends HttpServlet {
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

  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    res.getWriter().write("Valid URL!");
  }

  private boolean isBodyValid(Swipe swipe){
    if(swipe.getSwiper() > 5000 || swipe.getSwiper() < 1){
      return false;
    } else if (swipe.getSwipee() < 1 ||  swipe.getSwipee() > 1000000){
      return false;
    } else if (swipe.getComment().length() > 256){
      return false;
    }
    return true;
  }

  private boolean isUrlValid(String urlPath) {
    return urlPath.equals("/left/") || urlPath.equals("/right/");
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
//    http://localhost:8080/Twinder2_server_war_exploded/
    res.setContentType("text/plain");
    String urlPath = req.getPathInfo();
    Gson gson = new Gson();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("missing parameters [" + urlPath + "]");
      return;
    }

    if (!isUrlValid(urlPath)) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("Invalid URL!");
      return;
    }

    Swipe swipe = null;
    try {
      StringBuilder sb = new StringBuilder();
      String s;
      while ((s = req.getReader().readLine()) != null) {
        sb.append(s);
      }
      String leftright = urlPath.substring(1,urlPath.length()-1);
      swipe = (Swipe) gson.fromJson(sb.toString(), Swipe.class);
      swipe.setLeftorright(leftright);

    } catch (Exception ex) {
      ex.printStackTrace();
      res.getOutputStream().print(gson.toJson("Fail to get request body"));
      res.getOutputStream().flush();
    }

    if(swipe == null || !isBodyValid(swipe)){
      System.out.println(swipe.getSwipee());
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().write("Invalid Request Body!");
    } else {
      Channel channel = channelPool.borrowObject();
      channel.exchangeDeclare(EXCHANGE, "fanout");

      String messageStr = gson.toJson(swipe);
      channel.basicPublish(EXCHANGE, "", null, messageStr.getBytes(StandardCharsets.UTF_8));
      System.out.println(" [x] Sent '" + messageStr + "'");
      try {
        channelPool.returnObject(channel);
      } catch (Exception e) {
        System.out.println("Failed to send message to RabbitMQ");
        e.printStackTrace();
      }
      res.setStatus(HttpServletResponse.SC_OK);
      System.out.println(gson.toJson(swipe));
      res.getWriter().write(gson.toJson(swipe));
    }
  }

}


