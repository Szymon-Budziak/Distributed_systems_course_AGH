package space;

import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Agency extends Communicator {
    private final String name;
    private long requestId;
    private final String ackQueueName;
    private String adminQueueName;

    public Agency(String name) {
        this.name = name;
        this.requestId = 0;
        this.ackQueueName = "ackQueue" + name;
    }

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("Enter your agency name: ");
        String agencyName = br.readLine();

        Agency agency = new Agency(agencyName);
        agency.run();
    }

    public void run() throws Exception {
        System.out.println("Starting new agency `" + this.name + "`");

        Channel channel = this.createChannel();
        channel.exchangeDeclare("globalExchange", BuiltinExchangeType.TOPIC);
        this.adminQueueName = this.declareQueues(channel, this.ackQueueName);
        this.bindQueues(channel);
        this.basicConsume(channel);

        try {
            BufferedReader br = new BufferedReader(new java.io.InputStreamReader(System.in));

            System.out.print("Accepting messages. Enter your message (person/cargo/satellite): ");
            String message;
            while ((message = br.readLine()) != null) {
                if (!message.equals("person") && !message.equals("cargo") && !message.equals("satellite")) {
                    System.out.println("Invalid job type `" + message + "`");
                    continue;
                }

                Job job = new Job(this.name, message, Long.toString(this.requestId++));

                System.out.println("Sending job to key `" + job.getCapability() + "`");
                channel.basicPublish("globalExchange", job.getCapability(), null, job.toString().getBytes());
                System.out.println("Accepting messages. Enter your message (person/cargo/satellite): ");
            }

        } catch (IOException e) {
            System.out.println("Stopping agency `" + this.name + "`");
        } finally {
            channel.queueUnbind(this.ackQueueName, "globalExchange", this.name);
            channel.queueDelete(this.ackQueueName);
        }
    }

    private void bindQueues(Channel channel) throws IOException {
        channel.queueBind(this.ackQueueName, "globalExchange", this.name);
        channel.queueBind(this.adminQueueName, "globalExchange", "admin.agency");
    }

    private void basicConsume(Channel channel) throws IOException {
        String agencyName = this.name;

        Consumer ackConsumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, StandardCharsets.UTF_8);
                JobAck jobAck = new JobAck(message);
                System.out.println("Agency `" + agencyName + "` received `" + jobAck + "`");

                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };
        Consumer adminConsumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, StandardCharsets.UTF_8);
                System.out.println("Admin says `" + message + "`");

                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };
        channel.basicConsume(this.ackQueueName, false, ackConsumer);
        channel.basicConsume(this.adminQueueName, false, adminConsumer);
    }
}