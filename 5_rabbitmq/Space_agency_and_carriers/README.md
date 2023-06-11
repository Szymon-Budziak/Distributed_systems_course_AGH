# Space agency and carriers using RabbitMQ

## Dependencies

- Java 18
- RabbitMQ >= 3.10.0 or Docker (to run RabbitMQ)

## Files explanation

### Admin

The Admin class is responsible for running an administrator program that communicates with other components using
RabbitMQ messaging system. The Admin class establishes a connection with RabbitMQ, declares an exchange, and creates
and binds a queue to the exchange. It then waits for user input from the command line, expecting input in the format
"carrier/agency/all". Based on the input key the program publishes the message to the corresponding RabbitMQ routing
key within the "globalExchange". The program continues to wait for new user input until it is terminated, then the
queue is unbound from the exchange and deleted.

### Agency

The Agency class represents an agency program that interacts with a RabbitMQ messaging system. The program prompts the
user to enter the agency name and creates an instance of the Agency class with the provided name. The run method
establishes a connection with RabbitMQ, declares exchanges and queues, and binds the queues to the appropriate routing
keys within the exchange. It then consumes messages from the acknowledgment queue and an admin queue, printing the
received messages to the console and acknowledging the messages once they have been processed. The user is expected to
enter one of three job types: "person", "cargo", or "satellite". If an invalid job type is entered, an error message is
displayed. Otherwise, a Job object is created with the agency name, job type, and a unique request ID. The job is then
published to the corresponding routing key within the "globalExchange". The program continues to wait for new user input
until it is terminated, then the queues are unbound from the exchange and deleted.

### Carrier

The Carrier class represents a carrier program that interacts with a RabbitMQ messaging system. The program prompts the
user to enter the carrier name, capability 1, and capability 2. The run method establishes a connection with RabbitMQ,
declares exchanges and queues, and binds the queues to the appropriate routing keys within the exchange. It sets the
channel's basic quality of service to 1, indicating that the carrier can only process one message at a time. When
a message is received, it is processed by the appropriate consumer based on the message's capability. The received job
is printed to the console, the message is acknowledged, and a JobAck object is published to the corresponding agency's
routing key within the "globalExchange". The program continues to wait for new messages until it is terminated. When
the program is terminated, the queues are unbound from the exchange and deleted.

## Running the whole project

1. Run RabbitMQ, the easiest way is to run using Docker:

```shell
docker run -it --rm --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3.11-management
```

2. Inside `src.space` open **Admin**, **Agency** and **Carrier** classes and run them by running `main` method in each
of them.

#### Credits

This project was created by Szymon Budziak.