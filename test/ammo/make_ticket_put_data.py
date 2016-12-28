import random
import string
import json
import sys


def create_get_tickets(eventId):
    return "GET||/events/" + eventId + "/tickets||get_events_tickets||"


def create_get_events():
    return "GET||/events||get_events||"

def create_get_event(eventId):
    return "GET||/events/" + eventId + "||get_event||"

def create_put_ticket(ticket):
    ticket["status"] = "sold" if (random.randint(0, 1) == 0)  else "free"
    return "PUT||/tickets/" + ticket["_id"]["$oid"] + "||put_ticket||" + json.dumps(ticket)

def create_get_ticket(ticketId):
    return "GET||/tickets/" + ticketId + "||get_ticket||"

def main():
    with open('events.txt') as json_events_data:
        with open('tickets.txt') as json_tickets_data:
            events = json.load(json_events_data)

            for json_ticket in json_tickets_data:
                event = random.choice(events)
                ticket = json.loads(json_ticket)
                if (random.randint(0, 4) == 0):
                    print create_put_ticket(ticket)

                if (random.randint(0, 1) == 0):
                    i = random.randint(0, 10000)
                    if (i % 5 == 0):
                        print create_get_tickets(event['id'])
                    elif (i % 7 == 0):
                        print create_get_ticket(ticket["_id"]["$oid"])
                    elif (i % 19):
                        print create_get_events()
                    else:
                        print create_get_event(event['id'])

if __name__ == "__main__":
    main()