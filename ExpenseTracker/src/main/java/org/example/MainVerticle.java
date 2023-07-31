package org.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {

        // Create a Router
        MongoDbConnection cnn= new MongoDbConnection();

        WebClientOptions options = new WebClientOptions()
                .setDefaultPort(443)
                .setSsl(true);
        WebClient client = WebClient.create(vertx, options);
        Routers routerInStart= new Routers(vertx,cnn,client);
        Router router=routerInStart.createRouters();
        // Create the HTTP server
        vertx.createHttpServer()
                // Handle every request using the router
                .requestHandler(router)
                // Start listening
                .listen(8888)
                // Print the port
                .onSuccess(server ->
                        System.out.println(
                                "HTTP server started on port " + server.actualPort()
                        )
                );
    }
}
