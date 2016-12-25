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
import com.fasterxml.jackson.databind.JsonNode;

import play.libs.Json;
import play.Logger;

import java.lang.Number;
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


public class Ticket {
    public String id;
    public String url;
    public String owner;
    public String eventId;
    public String eventUrl;
    public String eventName;
    public String details;
    public Number price;
    public String status;


    public static Ticket fromJson(String json) {
        //System.out.println(json);
        //System.out.println(ticketNode.asText());
        Ticket ticket = Json.fromJson(Json.parse(json), Ticket.class);
        if (ticket.id != null && ticket.url == null) {
            ticket.url = "/tickets/" + ticket.id;
        }
        return ticket;
    }

    public String toJson() {
        return Json.toJson(this).toString();
    }

    public Document toMongoDocument() {
        Document doc = new Document()
            .append("owner", owner == null ? "" : owner)
            .append("eventId", eventId == null ? "" : eventId)
            .append("eventName", eventName == null ? "" : eventName)
            .append("eventUrl", eventUrl == null ? "" : eventUrl)
            .append("details", details == null ? "" : details)
            .append("price", price == null ? 0 : price)
            .append("status", status == null ? "free" : status);

        if (id != null) {
            if (url == null) {
                doc.append("url", "/tickets/" + id);
            }
            doc.append("_id", new ObjectId(id));
        }

        return doc;
    }

}