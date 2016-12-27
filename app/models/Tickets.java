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
import org.bson.types.ObjectId;
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
        Logger.debug("Finding tickets");

        ticketsCollection.find().map(Document::toJson).map(Ticket::fromJson)
            .into(new ArrayList<Ticket>(),
                (ArrayList<Ticket> tickets, Throwable t)-> {
                    if (t != null) {
                        Logger.error("Error getting tickets", t);
                    }
                    result.complete(tickets);            
                }
            );

        return result;
    }

    public CompletableFuture<ArrayList<Ticket>> getAllTickets() {
        CompletableFuture<ArrayList<Ticket>> result = new CompletableFuture<>();
        Logger.info("Getting all tickets");
        ticketsCollection.find().into(new ArrayList<Document>(),
            (ArrayList<Document> docs, Throwable t)-> {
                if (t != null) {
                    Logger.error("Error getting tickets for event", t);
                }
                ArrayList<Ticket> tickets = new ArrayList<>();
                if (docs != null) {                    
                    docs.forEach(doc -> {
                        Ticket ticket = Ticket.fromJson(doc.toJson());
                        ticket.id = doc.getObjectId("_id").toString();
                        if (ticket.id != null && ticket.url == null) {
                            ticket.url = "/tickets/" + ticket.id;
                        }

                        tickets.add(ticket);
                    });
                }
                result.complete(tickets);
            });
        return result;   
    }

    public CompletableFuture<ArrayList<Ticket>> getTicketsForEvent(String eventId) {
        CompletableFuture<ArrayList<Ticket>> result = new CompletableFuture<>();
        Logger.info("Finding tickets for event {}", eventId);
        ticketsCollection.find(eq("eventId", eventId)).into(new ArrayList<Document>(),
            (ArrayList<Document> docs, Throwable t)-> {
                if (t != null) {
                    Logger.error("Error getting tickets for event", t);
                }
                ArrayList<Ticket> tickets = new ArrayList<>();
                if (docs != null) {                    
                    docs.forEach(doc -> {
                        Ticket ticket = Ticket.fromJson(doc.toJson());
                        ticket.id = doc.getObjectId("_id").toString();
                        if (ticket.id != null && ticket.url == null) {
                            ticket.url = "/tickets/" + ticket.id;
                        }

                        tickets.add(ticket);
                    });
                }
                result.complete(tickets);
            });
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
                    Logger.debug("Ticket to create: {}", t);
                    ticketDocuments.add(t.toMongoDocument());
                });

                Logger.info("Inserting tickets.");

                ticketsCollection.insertMany(ticketDocuments, 
                    (Void res, final Throwable t) -> {
                        if (t != null) {
                            Logger.error("Tickets for event {} were not created.", eventId, t);
                            result.complete("error");
                        } else {                            
                            Logger.info("Tickets for event {} were created.", eventId);
                            result.complete("success");
                        }
                    }
                );
            } else {
                Logger.warn("Event with id {} was not found for creating tickets.", eventId);

                result.complete("event was not found");
            }      
            return event;  
        });

        return result;
    }

    public CompletableFuture<String> tryToBookTicket(Ticket ticket) {
        CompletableFuture<String> result = new CompletableFuture<>();

        ticketsCollection.findOneAndUpdate(
            and(eq("_id", new ObjectId(ticket.id)), eq("status", "free")),
            new Document("$set", ticket.toMongoDocument()),
            (Document doc, Throwable t) -> {
                if (t != null) {
                    Logger.error("Error updating ticket {}", ticket.id, t);
                }

                if (doc != null) {
                    result.complete("success");
                } else {
                    result.complete("error");
                }
            }
        );
        return result;
    }

    public CompletableFuture<Ticket> getById(String id) {

        CompletableFuture<Ticket> result = new CompletableFuture<>();

        ticketsCollection.find(eq("_id", new ObjectId(id))).first(
                (Document doc, Throwable t)-> {
                    Ticket ticket = null;
                    if (t != null) {
                        Logger.error("Error getting ticket by id", t);
                    }
                    if (doc != null) {
                        Logger.debug("Get ticket successfully: {}", id);
                        ticket = Ticket.fromJson(doc.toJson());
                        ticket.id = doc.getObjectId("_id").toString();
                    } else {
                        Logger.warn("Get ticket failed: {}", id);
                    }
                    result.complete(ticket != null ? Ticket.fromJson(ticket.toJson()) : null);            
                }
            );

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