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

public class MongoDbConnection {
    private MongoClient client;
    private MongoDatabase database;
    private MongoCollection<Document> collection;

    public MongoDbConnection(){
        client= MongoClients.create(dotenv.get("db"));
        database=client.getDatabase("ExpenseTracker");
    }
    public void InsertUser(Document doc, CheckUserCallBack callback){
        collection=database.getCollection("users");
        collection.insertOne(doc, new SingleResultCallback<Void>() {
            @Override
            public void onResult(Void result, Throwable t) {
                if(t!=null){
                    callback.onResult(false,t);
                }
                else{
                    boolean userCreated=true;
                    callback.onResult(userCreated,null);
                }
            }
        });
    }
    public void postExpense(List<Document> doc,String userId, PostSuccesfull callback){
        ObjectId objectId= new ObjectId(userId);
        collection=database.getCollection("users");
        Document filter = new Document("_id", objectId);
        Document update = new Document("$push", new Document("Expenses", new Document("$each", doc)));
        collection.updateOne(filter, update, new SingleResultCallback<UpdateResult>() {
            @Override
            public void onResult(UpdateResult result, Throwable t) {
                if(t!=null){
                    callback.onResult(false,t);
                }
                else{
                    boolean postCreated=true;
                    callback.onResult(postCreated,null);
                }
            }
        });

    }
    public void getReport(Document filter,LocalDate dateStartFormatted,LocalDate dateEndFormatted,ObjectId id, ResultCallBack resultCallBack){
          List<Document> resultList = new ArrayList<>();
        collection=database.getCollection("users");
        SingleResultCallback<Void> callbackWhenFinished = new SingleResultCallback<Void>() {
            @Override
            public void onResult(final Void result, final Throwable t) {
                resultCallBack.handleResult(resultList);
            }
        };
        Block<Document> printBlock = new Block<Document>() {
            @Override
            public void apply( Document document) {
                resultList.add(document);
            }
        };
        collection.aggregate(Arrays.asList(
                Aggregates.match(Filters.eq("_id",id)),
                Aggregates.unwind("$Expenses"),
                Aggregates.match(Filters.gt("Expenses.submittedDate",dateStartFormatted)),
                Aggregates.match(Filters.lt("Expenses.submittedDate",dateEndFormatted))
        )).forEach(printBlock, callbackWhenFinished);
    }

    public void getExpenses(ObjectId id ,LocalDate start , LocalDate end, ResultCallBack resultCallBack){
        List<Document> resultExpenses = new ArrayList<>();
        collection=database.getCollection("users");
        SingleResultCallback<Void> callbackWhenFinished = new SingleResultCallback<Void>() {
            @Override
            public void onResult(final Void result, final Throwable t) {
                resultCallBack.handleResult(resultExpenses);
            }
        };
        Block<Document> printBlock = new Block<Document>() {
            @Override
            public void apply( Document document) {resultExpenses.add(document);
            }
        };
        collection.aggregate(Arrays.asList(
                Aggregates.match(Filters.eq("_id",id)),
                Aggregates.unwind("$Expenses"),
                Aggregates.match(Filters.gt("Expenses.submittedDate",start)),
                Aggregates.match(Filters.lt("Expenses.submittedDate",end))
        )).forEach(printBlock, callbackWhenFinished);

    }





}
