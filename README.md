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
    "ticketsUrl": {
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

**/events**

**/event/:id/tickets**

**/user/:id/tickets**
