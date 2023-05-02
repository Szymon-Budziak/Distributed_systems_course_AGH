import grpc
import logging
import time
from concurrent import futures
from random import randint, choice

import event_notifier_pb2
import event_notifier_pb2_grpc

TEXT = {
    "full": "There are to many attendees. Try different event.",
    "success": "You have successfully subscribed to this event.",
    "already_there": "You are already subscribed to this event.",
    "not_found": "Wrong id. Event has not been found."
}


class EventServer(event_notifier_pb2_grpc.EventNotifierServicer):
    events_list = []
    client_id = 0

    def OnClientConnect(self, request, context):
        EventServer.client_id += 1
        print(f"Welcome client {request.client_name}! Your id is {EventServer.client_id}.")
        return event_notifier_pb2.OnClientConnectResponse(events_list=EventServer.events_list,
                                                          num_of_events=len(EventServer.events_list),
                                                          client_id=EventServer.client_id)

    def SubscribeOneEventById(self, request, context):
        time.sleep(2)
        for event in EventServer.events_list:
            if event.event_id == request.event_id:
                if event.max_attendees <= len(event.attendees):
                    return event_notifier_pb2.SubscribeOneEventByIdResponse(events_list=EventServer.events_list,
                                                                            event=event, text=TEXT["full"])
                elif request.client_id in event.attendees.keys():
                    return event_notifier_pb2.SubscribeOneEventByIdResponse(events_list=EventServer.events_list,
                                                                            event=event, text=TEXT["already_there"])
                event.attendees[request.client_id] = request.client_name
                return event_notifier_pb2.SubscribeOneEventByIdResponse(events_list=EventServer.events_list,
                                                                        event=event, text=TEXT["success"])
        return event_notifier_pb2.SubscribeOneEventByIdResponse(events_list=EventServer.events_list, event=None,
                                                                text=TEXT["not_found"])

    def SubscribeEventsByType(self, request, context):
        added = False
        subscribed_events = []
        for event in EventServer.events_list:
            if event.type == request.event_type:
                if event.max_attendees <= len(event.attendees):
                    continue
                elif request.client_id in event.attendees.keys():
                    continue
                event.attendees[request.client_id] = request.client_name
                subscribed_events.append(event)
                added = True
        time.sleep(2)
        if added:
            yield event_notifier_pb2.SubscribeEventsByTypeResponse(events_list=EventServer.events_list,
                                                                   subscribed_events=subscribed_events,
                                                                   text=TEXT["success"])
        else:
            yield event_notifier_pb2.SubscribeEventsByTypeResponse(events_list=EventServer.events_list,
                                                                   subscribed_events=subscribed_events,
                                                                   text=TEXT["not_found"])

    def SubscribeAllEvents(self, request, context):
        added = False
        subscribed_events = []
        for event in EventServer.events_list:
            if event.max_attendees <= len(event.attendees):
                continue
            elif request.client_id in event.attendees.keys():
                continue
            event.attendees[request.client_id] = request.client_name
            subscribed_events.append(event)
            added = True
        time.sleep(2)
        if added:
            yield event_notifier_pb2.SubscribeAllEventsResponse(events_list=EventServer.events_list,
                                                                subscribed_events=subscribed_events,
                                                                text=TEXT["success"])
        else:
            yield event_notifier_pb2.SubscribeAllEventsResponse(events_list=EventServer.events_list,
                                                                subscribed_events=subscribed_events,
                                                                text=TEXT["not_found"])


def create_default_events(nm_of_events):
    for i in range(nm_of_events):
        event = event_notifier_pb2.Event(
            event_id=str(i),
            type=choice(list(event_notifier_pb2.EventType.values())),
            attendees={0: "Admin"},
            max_attendees=randint(1, 8))
        EventServer.events_list.append(event)
    time.sleep(3)


def serve():
    port = '50051'
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    event_notifier_pb2_grpc.add_EventNotifierServicer_to_server(EventServer(), server)
    server.add_insecure_port('[::]:' + port)
    server.start()
    print("Server started, listening on " + port)

    create_default_events(3)

    server.wait_for_termination()


if __name__ == '__main__':
    logging.basicConfig()
    serve()
