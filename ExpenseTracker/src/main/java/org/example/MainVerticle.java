package org.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class MainVerticle extends AbstractVerticle {

    // This method will be executed when the verticle is deployed in the Vert.x instance.
    @Override
    public void start() throws Exception {

        // Create a connection to MongoDB
        MongoDbConnection cnn = new MongoDbConnection();

        // Create a WebClient with SSL support and default port 443
        WebClientOptions options = new WebClientOptions()
                .setDefaultPort(443)
                .setSsl(true);
        WebClient client = WebClient.create(vertx, options);

        // Create the Routers instance to configure the application's routers
        Routers routerInStart = new Routers(vertx, cnn, client);

        // Create and configure the application's Router
        Router router = routerInStart.createRouters();

        // Create the HTTP server
        vertx.createHttpServer()
                // Handle every request using the configured router
                .requestHandler(router)
                // Start listening on port 8888
                .listen(8888)
                // Print the port when the server starts successfully
                .onSuccess(server ->
                        System.out.println(
                                "HTTP server started on port " + server.actualPort()
                        )
                );
    }
}
