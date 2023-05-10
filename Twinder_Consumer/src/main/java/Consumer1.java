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
import com.google.gson.Gson;

public class Consumer1 {

  private final static String QUEUE_NAME = "Consumer1";
  private final static String EXCHANGE = "SwipeExchange";
  private final static int TOTALTHREADS = 80;
  private static HashMap<Integer, ArrayList<Integer>> likes = new HashMap<Integer, ArrayList<Integer>>();

  public static Boolean isLeft(String leftright){
    if (leftright == "left"){
      return true;
    } else {
      return false;
    }
  }
  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();

//    factory.setHost("ec2-34-211-231-25.us-west-2.compute.amazonaws.com");
//    factory.setPort(5672);
//    factory.setUsername("guest");
//    factory.setPassword("guest");
    factory.setHost("localhost");

    final Connection connection = factory.newConnection();

    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        try {
          final Channel channel = connection.createChannel();
          channel.queueDeclare(QUEUE_NAME, false, false, false, null);
          channel.exchangeDeclare(EXCHANGE, "fanout");
          channel.queueBind(QUEUE_NAME, EXCHANGE, "");
          channel.basicQos(1);
          System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");
          Gson gson = new Gson();
          DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            Swipe swipe = (Swipe) gson.fromJson(message.toString(), Swipe.class);
            Integer id = swipe.getSwiper();
//            dislike/left = index 0, like/right = index 1
            if(likes.containsKey(id)){
              ArrayList<Integer> count = likes.get(id);
              if(isLeft(swipe.getLeftorright())){
                int curr = count.get(0);
                likes.get(id).set(0, curr + 1);
              } else{
                int curr = count.get(1);
                likes.get(id).set(1, curr + 1);
              }
            } else {
              ArrayList<Integer> likedislike = new ArrayList<>(
                  List.of(0,0)
              );
              if(isLeft(swipe.getLeftorright())){
                likedislike.set(0, 1);
                likes.put(id, likedislike);
              } else {
                likedislike.set(1, 1);
                likes.put(id, likedislike);
              }
            }
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            System.out.println( "Callback thread ID = " + Thread.currentThread().getId() + " Received '" + message + "'");
          };
          channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });


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