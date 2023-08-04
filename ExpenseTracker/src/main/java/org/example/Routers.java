package org.example;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.AuthenticationHandler;
import io.vertx.ext.web.handler.BasicAuthHandler;
import io.vertx.ext.web.handler.BodyHandler;

public class Routers {
    private Router router;
    private AuthenticationHandler basicAuthHandler;
    private MyAuthenticator authentic;
    private MongoDbConnection cnn;
    private Handlers handlers;
    private WebClient client;

    // Constructor for the Routers class
    public Routers(Vertx vertx, MongoDbConnection cnn, WebClient client) {
        // Create a new Router
        this.router = Router.router(vertx);
        
        // Create a BasicAuthHandler with the provided MyAuthenticator instance
        authentic = new MyAuthenticator(cnn);
        basicAuthHandler = BasicAuthHandler.create(authentic);
        
        // Set up body handling for request processing
        router.route().handler(BodyHandler.create());
        
        // Store the provided MongoClient, MongoDbConnection, and WebClient instances
        this.cnn = cnn;
        this.client = client;

        // Create a new Handlers instance with the provided MongoDbConnection and WebClient instances
        handlers = new Handlers(cnn, client, vertx);
    }

    // Method to create the routes and return the Router
    public Router createRouters() {
        // Set up the failure handler for handling errors in the request processing
        router.route().failureHandler(handlers::handleFailures);
        
        // Set up the error handler for handling 404 errors (not found)
        router.errorHandler(404, handlers::handlerErrors);
        
        // Enable basic authentication for routes under the path "/track/"
        router.route("/track/*").handler(basicAuthHandler);
        
        // Define the routes and their corresponding handlers
        router.post("/track/createExpense").consumes("application/json").handler(handlers::createExpense);
        router.post("/createUser").handler(handlers::createUser);
        router.get("/track/getExpense").blockingHandler(handlers::getReport);
        router.get("/track/getPosts").handler(handlers::getPosts);
        
        // Return the Router
        return router;
    }
}
