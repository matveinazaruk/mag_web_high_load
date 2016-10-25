# mag_web_high_load

Сервис для заказа/бронирования каких-либо билетов

Существуют следующие сущности:
- пользователь
- событие
- билет 



**/event/:id**

Получает описание события, Схема:
``` json
{
  "title": "Event description",
  "type": "object",
  "properties": {
    "_self": {
      "description": "Link to event",
      "type": "string"
    },
    "name": {
      "description": "Event name",
      "type": "string"
    },
    "date": {
      "description": "Date if event",
      "type": "string",
      "format": "date-time"
    },
    "tickets": {
      "description": "Link to tickets",
      "type": "string"
    },
    "ticketsAmount": {
      "description": "Full tickets amount for event",
			"type": "integer",
      "minimum": 0
    },
    "freeTicketsAmount": {
      "description": "Full tickets amount for event",
			"type": "integer",
      "minimum": 0
    }
  }
}
```

**/tickets/:id**
Описание билета:

``` json
{
    "title": "Ticket description",
    "type" : "Object",
    "properties": {
        "_self": {
            "description": "Link to event",
            "type": "string"
        },
	"owner": {
	    "description": "Owner username",
	    "type": "String",
	   
	},
	"eventName": {
	    "description": "Name of event",
	    "type": "String",
	   
	},
	"event": {
	    "description": "Link to event",
	    "type": "String"
	},
	"price": {
	    "description": "Ticket price",
	    "type": "number"
	},
	"status": {
	  "description": "Ticket status",
	  "type": "string" 
	}
    }
}
```

**/events**

**/event/:id/tickets**

**/user/:id/tickets**
