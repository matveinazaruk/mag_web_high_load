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
import play.Logger;

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


public class Event {
	public String id;
	public String name;
	public String url;
	public String ticketsUrl;
	public ZonedDateTime date;
	public Integer ticketsAmount;
	public Integer freeTicketsAmount;

	public Event() {}

	public String getDate() {
		return date.toString();
	}

	public void setDate(String dateString) {
		date = ZonedDateTime.parse(dateString);
	}

	public static Event fromJson(String json) {
		//System.out.println(json);
		JsonNode eventNode = Json.parse(json);
		//System.out.println(eventNode.asText());
		return Json.fromJson(eventNode, Event.class);
	}

	public String toJson() {
		return Json.toJson(this).toString();
	}

	public Document toMongoDocument() {
        Document doc = new Document()
            .append("name", name)
            .append("date", date.toString())
            .append("ticketsAmount", ticketsAmount)
            .append("freeTicketsAmount", freeTicketsAmount);

        if (id != null) {
        	doc.append("_id", new ObjectId(id));
        }

    	return doc;
	}

}