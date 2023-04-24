from concurrent import futures
import logging
import grpc
import event_notifier_pb2, event_notifier_pb2_grpc
from random import randint, choice
import time

TEXT = {
    "full": "There is already to many attendees. Try different event.",
    "success": "You have successfully subscribed to this event.",
    "not_found": "Wrong id. Event has not been found."
}


class EventServer(event_notifier_pb2_grpc.EventNotifierServicer):
    def CreateDefaultEvents(self, request, context):
        events = []
        for i in range(request.num_of_events):
            events.append(event_notifier_pb2.Event(
                event_id=f'{i}',
                type=choice(list(event_notifier_pb2.EventType.values())),
                attendees=["Admin"],
                max_attendees=randint(1, 3)))
        time.sleep(3)
        yield event_notifier_pb2.DefaultEventsResponse(events_list=events, num_of_events=len(events))

    def SubscribeOneEventById(self, request, context):
        for event in request.events_list:
            if event.event_id == request.event_id:
                if event.max_attendees <= len(event.attendees):
                    return event_notifier_pb2.SubscribeOneEventByIdResponse(event=event, text=TEXT["full"])
                event.attendees.append(request.name)
                return event_notifier_pb2.SubscribeOneEventByIdResponse(event=event, text=TEXT["success"])
        time.sleep(2)
        return event_notifier_pb2.SubscribeOneEventByIdResponse(event=None, text=TEXT["not_found"])

    def SubscribeEventsByType(self, request, context):
        events = []
        for event in request.events_list:
            if event.type == request.event_type:
                if event.max_attendees <= len(event.attendees):
                    yield event_notifier_pb2.SubscribeEventsByTypeResponse(events_list=events, text=TEXT["full"])
                else:
                    event.attendees.append(request.name)
                    events.append(event)
        time.sleep(2)
        if len(events) > 0:
            yield event_notifier_pb2.SubscribeEventsByTypeResponse(events_list=events, text=TEXT["success"])
        else:
            yield event_notifier_pb2.SubscribeEventsByTypeResponse(events_list=events, text=TEXT["not_found"])

    def SubscribeAllEvents(self, request, context):
        events = []
        for event in request.events_list:
            if event.max_attendees <= len(event.attendees):
                yield event_notifier_pb2.SubscribeAllEventsResponse(events_list=events, text=TEXT["full"])
            else:
                event.attendees.append(request.name)
                events.append(event)
        time.sleep(2)
        if len(events) > 0:
            yield event_notifier_pb2.SubscribeAllEventsResponse(events_list=events, text=TEXT["success"])
        else:
            yield event_notifier_pb2.SubscribeAllEventsResponse(events_list=events, text=TEXT["not_found"])


def serve():
    port = '50051'
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    event_notifier_pb2_grpc.add_EventNotifierServicer_to_server(EventServer(), server)
    server.add_insecure_port('[::]:' + port)
    server.start()
    print("Server started, listening on " + port)
    server.wait_for_termination()


if __name__ == '__main__':
    logging.basicConfig()
    serve()
