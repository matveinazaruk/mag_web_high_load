package models;

import com.mongodb.Block;
import com.mongodb.ServerAddress;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.connection.ClusterSettings;
import com.mongodb.ConnectionString;
import org.bson.Document;
//import org.codehaus.jackson.JsonNode;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import play.libs.Json;
import play.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.time.ZonedDateTime;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.inc;
import static com.mongodb.client.model.Updates.set;
import static java.util.Arrays.asList;

public class Tickets {

	private static final String COLLECTION_NAME = "tickets";

	MongoCollection<Document> ticketsCollection;
    Events events;

	public Tickets(MongoDatabase db, Events events) {
		this.ticketsCollection = db.getCollection(COLLECTION_NAME);
        this.events = events;
	}

	public CompletableFuture<ArrayList> getTickets() {
        CompletableFuture<ArrayList> result = new CompletableFuture<>();

        ticketsCollection.find().map(Document::toJson).map(Ticket::fromJson)
            .into(new ArrayList<Ticket>(),
                (ArrayList<Ticket> tickets, Throwable throwable)-> {
                    result.complete(tickets);            
                }
            );

        return result;
	}

    public CompletableFuture<ArrayList<Ticket>> getTicketsForEvent(String eventId) {
        CompletableFuture<ArrayList<Ticket>> result = new CompletableFuture<>();
        Logger.info("Finding tickets for event {}", eventId);
        ticketsCollection.find(eq("eventId", eventId)).into(new ArrayList<Document>(),
            (ArrayList<Document> docs, Throwable throwable)-> {
                    ArrayList<Ticket> tickets = new ArrayList<>();
                    docs.forEach(doc -> {
                        Ticket ticket = Ticket.fromJson(doc.toJson());
                        ticket.id = doc.getObjectId("_id").toString();
                        tickets.add(ticket);
                    });
                    result.complete(tickets);
                }
            );
        return result;   
    }

	public CompletableFuture<String> addTicketsToEvent(List<Ticket> tickets, String eventId) {
        CompletableFuture<Event> eventToAddTickets = events.getById(eventId);

        CompletableFuture<String> result = new CompletableFuture<>();

        eventToAddTickets.thenApply((Event event) -> {
            if (event != null) {
                Logger.info("Found event {}, trying to create {} tickets.", eventId, tickets.size());
                List<Document> ticketDocuments = new ArrayList<Document>();

                tickets.forEach(t -> {
                    t.eventName = event.name;
                    t.eventUrl = event.url;
                    t.eventId = event.id;
                    t.status = "free";
                    Logger.info("Ticket to create: {}", t);
                    ticketDocuments.add(t.toMongoDocument());
                });

                Logger.info("Inserting tickets.");

                ticketsCollection.insertMany(ticketDocuments, 
                    (Void res, final Throwable t) -> {
                        Logger.info("Tickets for event {} were created.", eventId);
                        result.complete("success");
                    }
                );
                result.complete("event was found");

            } else {
                Logger.info("Event with id {} was not found for creating tickets.", eventId);

                result.complete("event was not found");
            }      
            return event;  
        });

		return result;
	}

    public List<Ticket> fromJson(String ticketsJson) {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayList<Ticket> tickets = null;
        try {
            tickets = objectMapper.readValue(
                            ticketsJson,
                            objectMapper.getTypeFactory().constructCollectionType(
                                    ArrayList.class, Ticket.class));
        } catch (JsonParseException e) {
            Logger.error("Tickets from json JsonParseException", e);
        } catch (JsonMappingException e) {
            Logger.error("Ticket from json JsonMappingException", e);
        } catch (IOException e) {
            Logger.error("Ticket from json IOException", e);
        }

        return tickets;
    }
}