# Event notifier in gRPC

## Dependencies

- Python >= 3.8
- gRPC
- Protobuf
- Node.js >= 14.0.0

## Directories explanation

### client

Client directory contains **client.js**, **package.json**, and **package-lock.json** files. The client.js file is a
Node.js code that communicates with a gRPC server using the event_notifier.proto protocol buffer. The client allows
users to subscribe to events by ID, events by type, or all events, and to receive updates about changes to the events.
Users can also close the connection when they are done. The client uses the readline interface to prompt users for
input, and updates a list of all events in real-time. The EventType object maps integers to event types for display
purposes.

### protos

Inside protos folder there is **event_notifier.proto** file that is a protobuf code for an event notifier service. The
service provides several gRPCs, including creating default events, subscribing to events by number, subscribing to events
by type, and subscribing to all events. The code also includes several message types, such as DefaultEventsRequest,
SubscribeOneEventByIdRequest, SubscribeEventsByTypeRequest, and Event. Additionally, the code includes an enum,
EventType, which lists the different types of events available.

### server

Server directory contains **server.py** file. The server.py file is a Python script that creates a gRPC server for
managing events and attendees. The server contains four methods: CreateDefaultEvents, SubscribeOneEventById,
SubscribeEventsByType, and SubscribeAllEvents.The CreateDefaultEvents method generates a specified number of default
events with random parameters, while the SubscribeOneEventById method allows attendees to subscribe to a single event by
ID. The SubscribeEventsByType method allows attendees to subscribe to all events of a given type, and the
SubscribeAllEvents method allows attendees to subscribe to all events. The server uses the event_notifier_pb2_grpc
module to communicate with clients and return event details and subscription results. The server runs on port 50051 and
uses a ThreadPoolExecutor with a maximum of 10 workers to handle requests. The server also contains some text messages
that are returned to clients based on the success or failure of their subscription attempts.

### Running the whole project

To run the whole project, you have to follow these steps:

1. Create event_notifier files for python code:

```bash
python3 -m grpc_tools.protoc -I protos/ --python_out=server/ --pyi_out=server/ --grpc_python_out=server/ protos/event_notifier.proto
```

2. Run server:

```bash
python3 server/server.py
```

3. Run client:

```bash
node client/client.js
```

#### Credits

This project was created by Szymon Budziak.