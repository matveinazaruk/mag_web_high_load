package controllers;

import models.Person;
import play.data.FormFactory;
import play.db.jpa.JPAApi;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;


import static play.libs.Json.toJson;

public class PersonController extends Controller {

    private Random random = new Random();

    @Inject
    public PersonController() {
    }

    public Result index() {

        int n = 10000 + random.nextInt(10000);
        int result = 0;
        for (int i = 0; i < n; i++) {
            result += i;
        }
        return ok("Got result: " + result);
        // return CompletableFuture.supplyAsync(() -> intensiveComputation())
                // .thenApply(i -> ok("Got result: " + i));
    }

    private int intensiveComputation() {
        int n = 10000 + random.nextInt(10000);
        int result = 0;
        for (int i = 0; i < n; i++) {
            result += i;
        }
        return result;
    }

}
