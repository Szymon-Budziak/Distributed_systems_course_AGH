package space;

import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Carrier extends Communicator {
    private final String name;
    private final String capability1;
    private final String capability2;
    private final String ackQueueName;
    private String adminQueueName;

    public Carrier(String name, String capability1, String capability2) {
        this.name = name;
        this.capability1 = capability1;
        this.capability2 = capability2;
        this.ackQueueName = "ackQueue" + name;
    }

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("Enter your carrier name: ");
        String carrierName = br.readLine();

        System.out.print("Enter your capability 1 (person/cargo/satellite): ");
        String capability1 = br.readLine();
        validateCapability(capability1);

        System.out.print("Enter your capability 2 (person/cargo/satellite): ");
        String capability2 = br.readLine();
        validateCapability(capability2);

        if (capability1.equals(capability2)) {
            throw new RuntimeException("Capability 1 and 2 cannot be the same.");
        }

        Carrier carrier = new Carrier(carrierName, capability1, capability2);
        carrier.run();
    }

    private static void validateCapability(String capability) {
        if (!capability.equals("person") && !capability.equals("cargo") && !capability.equals("satellite")) {
            throw new RuntimeException("Capability must be person, cargo, or satellite.");
        }
    }

    public void run() throws Exception {
        System.out.println("Starting new carrier " + this.name);

        Channel channel = this.createChannel();
        channel.exchangeDeclare("globalExchange", BuiltinExchangeType.TOPIC);
        this.adminQueueName = this.declareQueues(channel, this.ackQueueName);
        bindQueues(channel);

        channel.basicQos(1);
        System.out.println("Waiting for messages...");

        basicConsume(channel);
    }

    private void bindQueues(Channel channel) throws IOException {
        bindQueue(channel, this.capability1);
        bindQueue(channel, this.capability2);
        channel.queueBind(this.adminQueueName, "globalExchange", "admin.carrier");
    }

    private void bindQueue(Channel channel, String capability) throws IOException {
        String queueName = capability + "Queue";
        channel.queueBind(queueName, "globalExchange", capability);
    }

    private void basicConsume(Channel channel) throws IOException {
        String carrierName = this.name;

        Consumer taskConsumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, StandardCharsets.UTF_8);

                Job job = new Job(message);
                System.out.println("Received job: " + job);

                channel.basicAck(envelope.getDeliveryTag(), false);

                JobAck jobAck = new JobAck(carrierName, job.getRequestId());
                channel.basicPublish("globalExchange", job.getAgencyName(), null, jobAck.toString().getBytes());
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

        basicConsume(channel, this.capability1, taskConsumer);
        basicConsume(channel, this.capability2, taskConsumer);
        channel.basicConsume(this.adminQueueName, false, adminConsumer);
    }

    private void basicConsume(Channel channel, String capability, Consumer consumer) throws IOException {
        String queueName = capability + "Queue";
        channel.basicConsume(queueName, false, consumer);
    }
}