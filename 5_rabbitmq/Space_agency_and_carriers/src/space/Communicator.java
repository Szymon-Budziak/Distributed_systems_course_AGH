package space;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Communicator {
    protected Channel createChannel() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        return connection.createChannel();
    }

    protected String declareQueues(Channel channel, String ackQueueName) throws IOException {
        channel.queueDeclare("personQueue", false, false, false, null);
        channel.queueDeclare("cargoQueue", false, false, false, null);
        channel.queueDeclare("satelliteQueue", false, false, false, null);
        channel.queueDeclare(ackQueueName, false, false, false, null);

        return channel.queueDeclare().getQueue();
    }
}