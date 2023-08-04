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
import io.github.cdimascio.dotenv.Dotenv;

public class MyAuthenticator implements AuthProvider {
    private MongoDbConnection cnn;

    // Constructor for the MyAuthenticator class
    public MyAuthenticator(MongoDbConnection cnn) {
        this.cnn = cnn;
    }

    // Method to authenticate the user with provided credentials
    @Override
    public void authenticate(JsonObject authInfo, Handler<AsyncResult<User>> resultHandler) {
        // Retrieve username and password from the authInfo JsonObject
        String username = authInfo.getString("username");
        String password = authInfo.getString("password");

        // Create a Document object with the username and password for the query
        Document obj = new Document("name", username)
                .append("password", password);

        // Create a new MongoClient using the MongoDB connection string retrieved from the environment variable "db"
        MongoClient client = MongoClients.create(dotenv.get("db"));

        // Get the "ExpenseTracker" database from the MongoClient
        MongoDatabase database = client.getDatabase("ExpenseTracker");

        // Get the "users" collection from the database
        MongoCollection<Document> collection = database.getCollection("users");

        // Create a JsonObject to store user information
        JsonObject userInfo = new JsonObject();

        // Perform the query to find the user with provided credentials
        collection.find(obj).first((userDocument, throwable) -> {
            if (throwable != null || userDocument == null) {
                // If an error occurs or the user is not found, fail the authentication with an error message
                resultHandler.handle(io.vertx.core.Future.failedFuture("Invalid credentials"));
            } else {
                // If the user is found, extract the user's currency and id from the retrieved document
                String currency = userDocument.getString("currency");
                String id = userDocument.getObjectId("_id").toString();

                // Check the stored password against the provided password
                String storedP = userDocument.getString("password");

                // Put the user's id and currency into the userInfo JsonObject
                userInfo.put("id", id)
                        .put("currency", currency);

                // Create a User instance with the user information and pass it to the resultHandler
                User user = User.create(userInfo);
                resultHandler.handle(io.vertx.core.Future.succeededFuture(user));
            }
        });
    }
}
