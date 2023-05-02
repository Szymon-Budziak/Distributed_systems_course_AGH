const grpc = require('@grpc/grpc-js');
const protoLoader = require('@grpc/proto-loader');

const PROTO_PATH = __dirname + '/../protos/event_notifier.proto';
const packageDefinition = protoLoader.loadSync(
    PROTO_PATH,
    {
        keepCase: true,
        longs: String,
        enums: Event,
        defaults: true,
        oneofs: true
    });
const eventProto = grpc.loadPackageDefinition(packageDefinition).event_notifier;

const EventType = {
    0: 'Meeting',
    1: 'Conference',
    2: 'Festival',
    3: 'Sports',
};

const readline = require('readline').createInterface({
    input: process.stdin,
    output: process.stdout,
});

function getInputs(client, clientId, clientName) {
    readline.question('Do you want to subscribe to one event by id, events by type, to all events or to close the connection? (o/t/a/done): ', function (choice) {
        if (choice.toLowerCase() === 'o') {
            readline.question('Enter event id: ', function (eventId) {
                const request = {
                    client_id: clientId,
                    client_name: clientName,
                    event_id: eventId,
                };

                client.subscribeOneEventById(request, function (err, response) {
                    if (err) console.error(err);
                    else {
                        console.log(`${response.text} Event id ${response.event.event_id} of type ${EventType[response.event.type]}.`);
                        for (let event of response.events_list) {
                            console.log(`Event id ${event.event_id} of type ${EventType[event.type]} with ${Object.keys(event.attendees).length} attendees and max attendees ${event.max_attendees}.`);
                        }
                    }
                    getInputs(client, clientId, clientName);
                });
            });
        } else if (choice.toLowerCase() === 't') {
            readline.question('Enter event type: ', function (eventType) {
                eventType = eventType.toUpperCase();
                const request = {
                    client_id: clientId,
                    client_name: clientName,
                    event_type: eventType,
                };
                const stream = client.subscribeEventsByType(request);

                stream.on('data', function (events) {
                    for (let event of events.events_list) {
                        console.log(`${events.text} Event id ${event.event_id} of type ${EventType[event.type]}.`);
                        console.log(`Event id ${event.event_id} of type ${EventType[event.type]} with ${Object.keys(event.attendees).length} attendees and max attendees ${event.max_attendees}.`);

                    }
                });

                stream.on('end', function (events) {
                    getInputs(client, clientId, clientName);
                });
            });
        } else if (choice.toLowerCase() === 'a') {
            const request = {
                client_id: clientId,
                client_name: clientName,
            };
            const stream = client.subscribeAllEvents(request);

            stream.on('data', function (events) {
                for (let event of events.events_list) {
                    console.log(`${events.text} Event id ${event.event_id} of type ${EventType[event.type]}.`);
                        console.log(`Event id ${event.event_id} of type ${EventType[event.type]} with ${Object.keys(event.attendees).length} attendees and max attendees ${event.max_attendees}.`);

                }
            });

            stream.on('end', function () {
                for (let event of allEvents) {
                    console.log(`Event id ${event.event_id} of type ${EventType[event.type]} with ${event.attendees.length} attendees and max attendees ${event.max_attendees}.`);
                }
                getInputs(client, clientId, clientName);
            });
        } else if (choice.toLowerCase() === 'done') {
            console.log('See you next time.');
            readline.close()
        } else {
            console.log('Invalid choice. Repeat the choice.');
            getInputs(client, clientId, clientName);
        }
    });
}

function main() {
    const client = new eventProto.EventNotifier('localhost:50051', grpc.credentials.createInsecure());
    readline.question('Enter your name: ', function (name) {
        client.onClientConnect({client_name: name}, function (err, response) {
            if (err) console.log(err);
            else {
                console.log(`Welcome ${name}! Your id is ${response.client_id}.`);
                console.log(`There are ${response.num_of_events} events.`);
                for (let event of response.events_list) {
                    console.log(`Event id ${event.event_id} of type ${EventType[event.type]} with ${Object.keys(event.attendees).length} attendees and max attendees ${event.max_attendees}.`);
                }
                getInputs(client, response.client_id, name);
            }
        });
    });
}

main()