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

function updateAllEvents(events, event) {
    for (let ev of events) {
        if (ev.event_id === event.event_id) {
            for (let attendee of event.attendees) {
                if (!ev.attendees.includes(attendee)) {
                    ev.attendees.push(attendee);
                }
            }
            return;
        }
    }
}

const readline = require('readline').createInterface({
    input: process.stdin,
    output: process.stdout,
});

function getInputs(client, allEvents) {
    readline.question('Do you want to subscribe to one event by id, events by type, to all events or to close the connection? (o/t/a/done): ', function (choice) {
        if (choice.toLowerCase() === 'o') {
            readline.question('Enter your name: ', function (name) {
                readline.question('Enter event id: ', function (eventId) {
                    const request = {name: name, event_id: eventId, events_list: allEvents};

                    client.subscribeOneEventById(request, function (err, response) {
                        if (err) console.error(err);
                        else {
                            updateAllEvents(allEvents, response.event);
                            console.log(`${response.text} Event id ${response.event.event_id} of type ${EventType[response.event.type]}.`);
                            for (let event of allEvents) {
                                console.log(`Event id ${event.event_id} of type ${EventType[event.type]} with ${event.attendees.length} attendees and max attendees ${event.max_attendees}.`);
                            }
                        }
                        getInputs(client, allEvents);
                    });
                });
            });
        } else if (choice.toLowerCase() === 't') {
            readline.question('Enter your name: ', function (name) {
                readline.question('Enter event type: ', function (eventType) {
                    eventType = eventType.toUpperCase();
                    const request = {
                        name: name,
                        event_type: eventType,
                        events_list: allEvents,
                    };
                    const stream = client.subscribeEventsByType(request);

                    stream.on('data', function (events) {
                        for (let ev of events.events_list) {
                            updateAllEvents(allEvents, ev);
                            console.log(`${events.text} Event id ${ev.event_id} of type ${EventType[ev.type]}.`);
                        }
                    });

                    stream.on('end', function () {
                        for (let event of allEvents) {
                            console.log(`Event id ${event.event_id} of type ${EventType[event.type]} with ${event.attendees.length} attendees and max attendees ${event.max_attendees}.`);
                        }
                        getInputs(client, allEvents);
                    });
                });
            });
        } else if (choice.toLowerCase() === 'a') {
            readline.question("Enter your name: ", function (name) {
                const request = {name: name, events_list: allEvents};
                const stream = client.subscribeAllEvents(request);

                stream.on('data', function (events) {
                    for (let ev of events.events_list) {
                        updateAllEvents(allEvents, ev);
                        console.log(`${events.text} Event id ${ev.event_id} of type ${EventType[ev.type]}.`);
                    }
                });

                stream.on('end', function () {
                    for (let event of allEvents) {
                        console.log(`Event id ${event.event_id} of type ${EventType[event.type]} with ${event.attendees.length} attendees and max attendees ${event.max_attendees}.`);
                    }
                    getInputs(client, allEvents);
                });
            });
        } else if (choice.toLowerCase() === 'done') {
            console.log('See you next time.');
            readline.close()
        } else {
            console.log('Invalid choice. Repeat the choice.');
            getInputs(client, allEvents);
        }
    });
}

function main() {
    const client = new eventProto.EventNotifier('localhost:50051', grpc.credentials.createInsecure());
    const allEvents = [];

    const call = client.createDefaultEvents({num_of_events: 3});

    call.on('data', function (events) {
        console.log(`Generated ${events.num_of_events} events.`);
        for (let ev of events.events_list) {
            allEvents.push(ev);
            console.log(`Event id ${ev.event_id} of type ${EventType[ev.type]} with ${ev.attendees.length} attendees and max attendees ${ev.max_attendees}.`);
        }
    });

    call.on("end", function () {
            getInputs(client, allEvents);
        }
    )
}

main()