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


import java.util.ArrayList;
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
    public void postExpense(List<Document> doc, PostSuccesfull callback){
        collection=database.getCollection("expenses");
        collection.insertMany(doc, new SingleResultCallback<Void>() {
            @Override
            public void onResult(Void result, Throwable t) {
                if(t!=null){
                    callback.onResult(false,t);
                }
                else{
                    boolean postSucces=true;
                    callback.onResult(postSucces,null);
                }
            }
        });
    }
    public void getReport(Document filter, ResultCallBack resultCallBack){
        List<Document> resultList = new ArrayList<>();
        collection=database.getCollection("expenses");
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
        collection.find(filter).forEach(printBlock, callbackWhenFinished);
    }

    public void getExpenses(Document filter,ResultCallBack resultCallBack){
        List<Document> resultExpenses = new ArrayList<>();
        collection=database.getCollection("expenses");
        SingleResultCallback<Void> callbackWhenFinished = new SingleResultCallback<Void>() {
            @Override
            public void onResult(final Void result, final Throwable t) {
                resultCallBack.handleResult(resultExpenses);
            }
        };
        Block<Document> printBlock = new Block<Document>() {
            @Override
            public void apply( Document document) {
                resultExpenses.add(document);
            }
        };
        collection.find(filter).forEach(printBlock, callbackWhenFinished);

    }





}
