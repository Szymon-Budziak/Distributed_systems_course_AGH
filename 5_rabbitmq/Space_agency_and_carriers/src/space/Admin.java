package space;

import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class Admin extends Communicator {
    private String middlemanQueue;

    public static void main(String[] args) throws Exception {
        Admin admin = new Admin();
        admin.run();
    }

    public void run() throws Exception {
        Channel channel = this.createChannel();
        channel.exchangeDeclare("globalExchange", BuiltinExchangeType.TOPIC);
        this.declareAndBindQueues(channel);

        System.out.println("Admin is running.");

        this.basicConsume(channel);

        try {
            BufferedReader br = new BufferedReader(new java.io.InputStreamReader(System.in));
            System.out.print("Admin waiting for call! Enter message (in format agency/carrier/all): ");
            String message;
            while ((message = br.readLine()) != null) {
                String[] split = message.split(Pattern.quote(":"));

                if (!isFormatValid(split)) {
                    System.out.println("Invalid format.");
                    continue;
                }

                String key = split[0];
                StringBuilder toSendBuilder = new StringBuilder();
                for (int i = 1; i < split.length; ++i) {
                    toSendBuilder.append(split[i]);
                    if (i != split.length - 1) {
                        toSendBuilder.append(':');
                    }
                }

                byte[] toSend = toSendBuilder.toString().getBytes();

                switch (key) {
                    case "agency" -> channel.basicPublish("globalExchange", "admin.agency", null, toSend);
                    case "carrier" -> channel.basicPublish("globalExchange", "admin.carrier", null, toSend);
                    case "all" -> {
                        channel.basicPublish("globalExchange", "admin.carrier", null, toSend);
                        channel.basicPublish("globalExchange", "admin.agency", null, toSend);
                    }
                    default -> System.out.println("Invalid admin message key. Possible are agency/carrier/all.");
                }
                System.out.println("Admin waiting for call! Enter message (in format agency/carrier/all): ");
            }

        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        } finally {
            channel.queueUnbind(this.middlemanQueue, "globalExchange", "#");
            channel.queueDelete(this.middlemanQueue);
        }
    }

    private void declareAndBindQueues(Channel channel) throws IOException {
        this.middlemanQueue = channel.queueDeclare().getQueue();
        channel.queueBind(this.middlemanQueue, "globalExchange", "#");
    }

    private void basicConsume(Channel channel) throws IOException {
        Consumer middlemanConsumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, StandardCharsets.UTF_8);
                System.out.println("Found message: " + message);

                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };
        channel.basicConsume(this.middlemanQueue, false, middlemanConsumer);
    }

    private boolean isFormatValid(String[] split) {
        if (split.length < 2) {
            return false;
        }
        String sendTo = split[0];
        System.out.println("Send to: " + sendTo);

        return (sendTo.equals("carrier") || sendTo.equals("agency") || sendTo.equals("all"));
    }
}