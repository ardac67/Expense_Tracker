package org.example;

import InterFace.CheckUserCallBack;
import InterFace.PostSuccesfull;
import InterFace.ResultCallBack;
import com.mongodb.Block;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.*;
import com.mongodb.client.result.UpdateResult;
import io.vertx.core.json.JsonObject;
import org.bson.Document;
import org.bson.conversions.Bson;
import io.github.cdimascio.dotenv.Dotenv;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

public class MongoDbConnection {
    private MongoClient client;
    private MongoDatabase database;
    private MongoCollection<Document> collection;

    // Constructor for the MongoDbConnection class
    public MongoDbConnection() {
        // Create a new MongoClient using the MongoDB connection string retrieved from the environment variable "db"
        client = MongoClients.create(dotenv.get("db"));

        // Get the "ExpenseTracker" database from the MongoClient
        database = client.getDatabase("ExpenseTracker");
    }

    // Method to insert a user document into the "users" collection
    public Future<Void> InsertUser(Document doc) {
        Promise<Void> promise = Promise.promise();
        try {
            // Get the "users" collection from the database
            collection = database.getCollection("users");

            // Insert the provided document into the "users" collection
            collection.insertOne(doc, (result, t) -> {
                if (t == null) {
                    // If successful, complete the promise with no value
                    promise.complete();
                } else {
                    // If an error occurs, fail the promise with the error message
                    promise.fail(t.getMessage());
                }
            });

            // Return the future associated with the promise
            return promise.future();
        } catch (Exception ex) {
            // If an exception occurs, fail the promise with the exception message
            promise.fail(ex.getMessage());
            return promise.future();
        }
    }

    // Method to add expenses to a user document in the "users" collection
    public Future<Void> postExpense(List<Document> doc, String userId) {
        Promise<Void> promise = Promise.promise();
        try {
            // Create an ObjectId instance from the provided userId
            ObjectId objectId = new ObjectId(userId);

            // Get the "users" collection from the database
            collection = database.getCollection("users");

            // Define the update operation to push the provided documents into the "Expenses" array
            Document filter = new Document("_id", objectId);
            Document update = new Document("$push", new Document("Expenses", new Document("$each", doc)));

            // Perform the update operation on the document in the collection
            collection.updateOne(filter, update, (result, t) -> {
                if (t != null) {
                    // If an error occurs, fail the promise with the error message
                    promise.fail(t.getMessage());
                } else {
                    // If successful, complete the promise with no value
                    promise.complete();
                }
            });

            // Return the future associated with the promise
            return promise.future();
        } catch (Exception ex) {
            // If an exception occurs, fail the promise with the exception message
            promise.fail(ex.getMessage());
            return promise.future();
        }
    }

    // Method to retrieve expense documents matching the provided filter within a date range for a specific user
    public Future<List<Document>> getReport(Document filter, LocalDate dateStartFormatted, LocalDate dateEndFormatted, ObjectId id) {
        Promise<List<Document>> promise = Promise.promise();
        try {
            // Create an empty list to store the results
            List<Document> resultList = new ArrayList<>();

            // Get the "users" collection from the database
            collection = database.getCollection("users");

            // Define a callback that will be called when the aggregation query is finished
            SingleResultCallback<Void> callbackWhenFinished = (result, t) -> {
                try {
                    // When the aggregation is finished, complete the promise with the resultList
                    promise.complete(resultList);
                } catch (Exception ex) {
                    // If an exception occurs, fail the promise with the exception message
                    promise.fail(ex.getMessage());
                }
            };

            // Define a block to handle each document returned by the aggregation
            Block<Document> printBlock = document -> resultList.add(document);

            // Perform the aggregation query to retrieve the desired expense documents
            collection.aggregate(Arrays.asList(
                    Aggregates.match(Filters.eq("_id", id)),
                    Aggregates.unwind("$Expenses"),
                    Aggregates.match(Filters.gt("Expenses.submittedDate", dateStartFormatted)),
                    Aggregates.match(Filters.lt("Expenses.submittedDate", dateEndFormatted))
            )).forEach(printBlock, callbackWhenFinished);

            // Return the future associated with the promise
            return promise.future();
        } catch (Exception ex) {
            // If an exception occurs, fail the promise with the exception message
            promise.fail(ex.getMessage());
            return promise.future();
        }
    }

}
