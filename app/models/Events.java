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
import com.fasterxml.jackson.databind.JsonNode;

import play.libs.Json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.HashSet;
import java.time.ZonedDateTime;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.inc;
import static com.mongodb.client.model.Updates.set;
import static java.util.Arrays.asList;

public class Events {

	private static final String COLLECTION_NAME = "events";

	MongoCollection<Document> eventsCollection;

	public Events(MongoDatabase db) {
		eventsCollection = db.getCollection("events");
	}

	public CompletableFuture<ArrayList> getEvents() {
        CompletableFuture<ArrayList> result = new CompletableFuture<>();

        eventsCollection.find().into(new ArrayList<Document>(),
                (ArrayList<Document> docs, Throwable throwable)-> {
                	ArrayList<Event> events = new ArrayList<>();
                	docs.forEach(doc -> {
                		Event event = Event.fromJson(doc.toJson());
                		event.id = doc.getObjectId("_id").toString();
            			events.add(event);
                	});
                    result.complete(events);
                }
            );

        return result;
	}


	public CompletableFuture<String> addEvent(Event event) {
        CompletableFuture<String> result = new CompletableFuture<>();
		eventsCollection.insertOne(event.toMongoDocument(), 
        	(Void res, final Throwable t) -> {
    			result.complete("success");
        	}
        );

		return result;
	}

	public CompletableFuture<Event> getById(String id) {
		// BasicDBObject eqId = new BasicDBObject();
		// eqId.put("_id", new ObjectId("585ae54d97446f0d315c2913"));

        CompletableFuture<Event> result = new CompletableFuture<>();

        eventsCollection.find(eq("_id", new ObjectId(id))).first(
                (Document doc, Throwable throwable)-> {
                	Event event = null;
                	if (doc != null) {
	            		event = Event.fromJson(doc.toJson());
	            		event.id = doc.getObjectId("_id").toString();
                	}
                    result.complete(event != null ? Event.fromJson(event.toJson()) : null);            
                }
            );

        return result;
	}
}