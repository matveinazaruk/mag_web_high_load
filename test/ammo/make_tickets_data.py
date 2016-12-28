import random
import string
import json
import sys


def randomword(length):
    return ''.join(random.choice(string.lowercase) for i in range(length))

def create_post_tickets(eventId, n):
    tickets = []
    for _ in xrange(n):
        ticket = {}
        ticket["details"] = "Ticket for " + eventId + " for place " + randomword(random.randint(5, 10)) 
        ticket["price"] = random.randint(100, 200)    
        tickets.append(ticket)
    return "POST||/events/" + eventId + "/tickets||add_events_tickets||" + json.dumps(tickets)

def main():
    with open('events.txt') as json_data:
        events = json.load(json_data)
        for event in events:
            print create_post_tickets(event['id'], event['ticketsAmount'])

if __name__ == "__main__":
    main()