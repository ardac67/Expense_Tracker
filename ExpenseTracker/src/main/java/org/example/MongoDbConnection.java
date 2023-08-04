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

    public MongoDbConnection(){
        client= MongoClients.create(dotenv.get("db"));
        database=client.getDatabase("ExpenseTracker");
    }
    public Future<Void> InsertUser(Document doc){
        Promise<Void> promise = Promise.promise();
        try {
            collection = database.getCollection("users");
            collection.insertOne(doc, (result, t) -> {
                if (t == null) {
                    promise.complete();
                } else {
                    promise.fail(t.getMessage());
                }
            });
            return promise.future();
        } catch(Exception ex){
            promise.fail(ex.getMessage());
            return promise.future();
        }
    }
    public Future<Void> postExpense(List<Document> doc,String userId){
        Promise<Void> promise= Promise.promise();
        try {
            ObjectId objectId = new ObjectId(userId);
            collection = database.getCollection("users");
            Document filter = new Document("_id", objectId);
            Document update = new Document("$push", new Document("Expenses", new Document("$each", doc)));
            collection.updateOne(filter, update, (result, t) -> {
                if (t != null) {
                    promise.fail(t.getMessage());
                } else {
                    promise.complete();
                }
            });
            return promise.future();
        }catch(Exception ex){
            promise.fail(ex.getMessage());
            return promise.future();
        }
    }
    public Future<List<Document>> getReport(Document filter,LocalDate dateStartFormatted,LocalDate dateEndFormatted,ObjectId id){
        Promise<List<Document>> promise= Promise.promise();
        try {
            List<Document> resultList = new ArrayList<>();
            collection = database.getCollection("users");
            SingleResultCallback<Void> callbackWhenFinished = (result, t) ->{
                try {
                    promise.complete(resultList);
                }
                catch(Exception ex){
                    promise.fail(ex.getMessage());
                }
            };
            Block<Document> printBlock = document -> resultList.add(document);
            collection.aggregate(Arrays.asList(
                    Aggregates.match(Filters.eq("_id", id)),
                    Aggregates.unwind("$Expenses"),
                    Aggregates.match(Filters.gt("Expenses.submittedDate", dateStartFormatted)),
                    Aggregates.match(Filters.lt("Expenses.submittedDate", dateEndFormatted))
            )).forEach(printBlock, callbackWhenFinished);
            return promise.future();
        }
        catch(Exception ex){
            promise.fail(ex.getMessage());
            return promise.future();
        }
    }

}
