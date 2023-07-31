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
    public  Routers(Vertx _vertx, MongoDbConnection _cnn, WebClient _client){
        router = Router.router(_vertx);
        client=_client;
        router.route().handler(BodyHandler.create());
        cnn=_cnn;
        authentic= new MyAuthenticator(cnn);
        basicAuthHandler= BasicAuthHandler.create(authentic);
        handlers= new Handlers(cnn,client,_vertx);
    }
    public Router createRouters(){
        router.route().failureHandler(handlers::handleFailures);
        router.errorHandler(404,handlers::handlerErrors);
        router.route("/track/*").handler(basicAuthHandler);
        router.post("/track/createExpense").consumes("application/json").handler(handlers::createExpense);
        router.post("/createUser").handler(handlers::createUser);
        router.get("/track/getExpense").blockingHandler(handlers::getReport);
        router.get("/track/getPosts").handler(handlers::getPosts);
        //router.delete("/track/deletePosts").handler(handlers::deletePost);
        //router.put("/track/editPost").handler(handlers::editPost);
        return router;
    }
}
