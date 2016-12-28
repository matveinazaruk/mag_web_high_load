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

def main():
    with open('events.txt') as json_data:
        events = json.load(json_data)
        for i, event in enumerate(events):
            if (i % 5 == 0):
                print create_get_tickets(event['id'])
            elif (i % 19):
                print create_get_events()
            else:
                print create_get_event(event['id'])


if __name__ == "__main__":
    main()