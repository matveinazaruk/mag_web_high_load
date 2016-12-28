import random
import string
import json
import sys


def randomword(length):
   return ''.join(random.choice(string.lowercase) for i in range(length))

def create_post_event(i):
    event = {}
    event["name"] = "Event E" + str(i) + " " + randomword(random.randint(5, 10))
    event["date"] = "2016-10-27T15:00+03:00"    
    event["freeTicketsAmount"] = event["ticketsAmount"] = random.randint(100, 200)
    return "POST||/events||add_event||" +json.dumps(event)

def main():
    for i in xrange(1, 5000):
        print create_post_event(i)

if __name__ == "__main__":
    main()