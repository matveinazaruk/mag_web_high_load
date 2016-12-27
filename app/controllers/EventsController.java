package controllers;

import play.data.FormFactory;
import play.cache.Cached;
import play.db.jpa.JPAApi;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.BodyParser;

import com.mongodb.Block;
import com.mongodb.ServerAddress;
import com.mongodb.async.client.MongoClientSettings;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.connection.ClusterSettings;
import com.mongodb.connection.ConnectionPoolSettings;
import com.mongodb.ConnectionString;
import com.mongodb.ReadPreference;
import org.bson.Document;

import javax.inject.Inject;
import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
import java.util.stream.Collectors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import play.libs.Json;
import play.Logger;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.inc;
import static com.mongodb.client.model.Updates.set;
import static java.util.Arrays.asList;


import models.Event;
import models.Events;
import models.Ticket;
import models.Tickets;


public class EventsController extends Controller {
    private static final String DB_NAME = "eventsdb";
    private static final String CONNECTION_STRING =
        "mongodb://207.154.192.51:27017,138.68.110.78:27017,207.154.202.219:27017/?replicaSet=eventsrepl&connectTimeoutMS=300000&readPreference=nearest";

    private static final List<String> USERS = new ArrayList(asList("matthew", "andrew", "bob", "samuel"));

    private Random random = new Random();
    MongoClient mongoClient;
    MongoDatabase database;
    Events events;
    Tickets tickets;

    @Inject
    public EventsController() {
        List servers = asList(
            new ServerAddress("207.154.192.51:27017"),
            new ServerAddress("138.68.110.78:27017"),
            new ServerAddress("207.154.202.219:27017")
        );
        ClusterSettings clusterSettings = ClusterSettings.builder()
            .hosts(servers)
            .requiredReplicaSetName("eventsrepl")
            .description("Events replica set")
            .build();

        ConnectionPoolSettings connectionPoolSettings = ConnectionPoolSettings.builder()
            .maxWaitQueueSize(1500)
            .maxWaitTime(30, TimeUnit.SECONDS)
            .build();

        MongoClientSettings settings = MongoClientSettings.builder()
            .clusterSettings(clusterSettings)
            .connectionPoolSettings(connectionPoolSettings)
            .build();

        mongoClient = MongoClients.create(settings);
        database = mongoClient.getDatabase(DB_NAME);
        events = new Events(database);
        tickets = new Tickets(database, events);
    }

    @Cached(key = "events_list")
    public CompletableFuture<Result> events() {
        CompletableFuture result = events.getEvents();
        return result.thenApply((evs) -> ok(Json.toJson(evs)));
    }

    @BodyParser.Of(BodyParser.TolerantJson.class)
    public CompletableFuture<Result> createEvent() {

        Event event = Event.fromJson(request().body().asJson().toString());
        CompletableFuture result = events.addEvent(event);

        return result.thenApply((res) -> created(res.toString()));

    }

    @BodyParser.Of(BodyParser.TolerantJson.class)
    public CompletableFuture<Result> createTickets(String eventId) {

        List<Ticket> ticketsToCreate = tickets.fromJson(request().body().asJson().toString());
        CompletableFuture<String> result = tickets.addTicketsToEvent(ticketsToCreate, eventId);

        return result.thenApply((res) -> created(res));

    }

    @BodyParser.Of(BodyParser.TolerantJson.class)
    public CompletableFuture<Result> updateTicket(String ticketId) {

        Ticket ticketToUpdate = Ticket.fromJson(request().body().asJson().toString());
        ticketToUpdate.id = ticketId;
        int ownerIdx = ThreadLocalRandom.current().nextInt(USERS.size());
        ticketToUpdate.owner = USERS.get(ownerIdx);
        System.out.println(ticketToUpdate.toJson());
        CompletableFuture<String> result = tickets.tryToBookTicket(ticketToUpdate);
        // CompletableFuture<String> result = tickets.addTicketsToEvent(ticketsToCreate, eventId);
        return result.thenApply((res) -> ok(res));
        // return result.thenApply((res) -> created(res));

    }

    public CompletableFuture<Result> getTickets(String eventId) {

        CompletableFuture<ArrayList<Ticket>> result = tickets.getTicketsForEvent(eventId);

        return result.thenApply((ticks) -> ok(ticks != null ? Json.toJson(ticks).toString() : "Not found"));

    }

    public CompletableFuture<Result> getTicket(String ticketId) {

        CompletableFuture<Ticket> result = tickets.getById(ticketId);

        return result.thenApply((tick) -> ok(tick != null ? tick.toJson() : "Not found"));

    }

    public CompletableFuture<Result> getEvent(String eventId) {

        CompletableFuture<Event> result = events.getById(eventId);

        return result.thenApply((evt) -> ok(evt != null ? evt.toJson() : "Not found"));

    }
}
