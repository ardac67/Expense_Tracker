package org.example;

import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import org.bson.Document;


public class MyAuthenticator implements AuthProvider {
    private MongoDbConnection conn;

    public MyAuthenticator(MongoDbConnection cnn){
        conn=cnn;
    }

    @Override
    public void authenticate(JsonObject authInfo, Handler<AsyncResult<User>> resultHandler) {
        conn = new MongoDbConnection();
        String username = authInfo.getString("username");
        String password = authInfo.getString("password");

        Document obj = new Document("name", username)
                .append("password",password);
        MongoClient client= MongoClients.create("mongodb://cartcurt");
        MongoDatabase  database=client.getDatabase("ExpenseTracker");
        MongoCollection<Document> collection=database.getCollection("users");
        JsonObject userInfo= new JsonObject();

        collection.find(obj).first((userDocument, throwable) -> {
            if (throwable != null || userDocument == null) {
                resultHandler.handle(io.vertx.core.Future.failedFuture("Invalid credentials"));
            } else {
                String currency = userDocument.getString("currency");
                String id = userDocument.getObjectId("_id").toString(); //
                String storedP=userDocument.getString("password");
                if (storedP.equals(password)) {
                    userInfo.put("id",id)
                            .put("currency",currency);
                    User user = User.create(userInfo);
                    resultHandler.handle(io.vertx.core.Future.succeededFuture(user));
                } else {
                    resultHandler.handle(io.vertx.core.Future.failedFuture("Invalid credentials"));
                }
            }
        });
    }
}
