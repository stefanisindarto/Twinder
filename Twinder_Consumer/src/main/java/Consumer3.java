import DAO.SwipeDataDao;
import Model.Swipe;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Consumer3 {

  private final static String QUEUE_NAME = "Consumer3";
  private final static String EXCHANGE = "SwipeExchange";
  private final static int TOTALTHREADS = 66;
  private static ArrayList<Swipe> swipeData = new ArrayList<Swipe>();
  private static HashMap<Integer, List<Integer>> matches = new HashMap<Integer, List<Integer>>();

  public static Boolean isLeft(String leftright){
    if (leftright == "left"){
      return true;
    } else {
      return false;
    }
  }
  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("ec2-34-211-231-25.us-west-2.compute.amazonaws.com");
    factory.setPort(5672);
    factory.setUsername("guest");
    factory.setPassword("guest");
//        factory.setHost("localhost");
    final Connection connection = factory.newConnection();

    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        try {
          final Channel channel = connection.createChannel();
          channel.queueDeclare(QUEUE_NAME,true, false, false, null);
          channel.exchangeDeclare(EXCHANGE, "fanout");
          channel.queueBind(QUEUE_NAME, EXCHANGE, "");
          channel.basicQos(1);
          System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");
          Gson gson = new Gson();
          DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            Swipe swipe = (Swipe) gson.fromJson(message.toString(), Swipe.class);
            SwipeDataDao swipeDataDao = new SwipeDataDao();
            swipeDataDao.createSwipeData(swipe);

            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            System.out.println( "Callback thread ID = " + Thread.currentThread().getId() + " Received '" + message + "'");
          };
          channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });


        } catch (IOException ex) {
          Logger.getLogger(Consumer1.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    };

    for (int i = 0; i <TOTALTHREADS ; i++) {
      Thread receive = new Thread(runnable);
      receive.start();
    }

    }
}
