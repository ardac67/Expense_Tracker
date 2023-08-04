package org.example;

import InterFace.CheckUserCallBack;
import InterFace.PostSuccesfull;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import org.bson.Document;
import org.bson.types.ObjectId;
import io.github.cdimascio.dotenv.Dotenv;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

public class Handlers {
    private MongoDbConnection cnn;
    private WebClient client;
    private Vertx vertx;
    private String baseCurrency;
    private JsonArray jsonLegacyArray;
    private JsonObject currencyData;
    private Set<String> validLegacyCodes ;
    public Handlers(MongoDbConnection cnn,WebClient client,Vertx vertx)
    {
        this.cnn=cnn;
        this.client=client;
        this.vertx=vertx;
        getLegacyCode();
    }
    public void createUser(RoutingContext routingContext) {
        try {
            JsonObject obj = routingContext.getBodyAsJson();
            Document document = new Document("name", obj.getString("name"))
                    .append("password", obj.getString("password"))
                    .append("currency", obj.getString("currency"));
            Future<Void> future = cnn.insertUser(document);
            future.onComplete(ar -> {
                if (ar.failed()) {
                    routingContext.response().end("error happened");
                } else {
                    routingContext.response().setStatusCode(201).end(new JsonObject().put("response", "successful").encodePrettily());
                }
            });
        }
        catch(Exception ex){
            routingContext.response().setStatusCode(500).end("error occurred");
        }
    }

    public void handlerErrors(RoutingContext routingContext) {
        JsonObject errorMessage= new JsonObject();
        errorMessage.put("error",
                new JsonObject().put("code",404)
                        .put("message","no matching routes")
        );
        routingContext.response().setStatusCode(404).end(errorMessage.encodePrettily());
    }

    public void handleFailures(RoutingContext routingContext) {
        int statusCode = routingContext.statusCode();
        JsonObject errorMessage= new JsonObject();
        if(statusCode==500){
            errorMessage.put("error",
                    new JsonObject().put("code",statusCode)
                            .put("message","runtimeError"));
            routingContext.response().setStatusCode(500).end(errorMessage.encodePrettily());
        }
        else if(statusCode==401) {
            errorMessage.put("error",
                    new JsonObject().put("code",statusCode)
                            .put("message","unauthorized"));
            routingContext.response().setStatusCode(401).end(errorMessage.encodePrettily());
        }
        else{
            errorMessage.put("error",
                    new JsonObject().put("code",statusCode)
                            .put("message","bad request"));
            routingContext.response().setStatusCode(400).end(errorMessage.encodePrettily());
        }
    }

    public void createExpense(RoutingContext routingContext) {
        try {
            DateTimeBuilder builder = new DateTimeBuilder();
            JsonArray cntxType = routingContext.body().asJsonArray();
            String userId = routingContext.user().principal().getString("id");
            List<Document> docList = new ArrayList<>();
            for (int i = 0; i < cntxType.size(); i++) {
                Document document = new Document();
                JsonObject obj = cntxType.getJsonObject(i);
                Set<String> fieldSet = obj.getJsonObject("expenseType").fieldNames();
                List<String> fieldNames = new ArrayList<>(fieldSet);
                for (String fieldName : fieldNames) {
                    document
                            .append(fieldName, obj.getJsonObject("expenseType").getDouble(fieldName));
                }
                LocalDate dateStart = builder.returnFormattedDate(obj.getString("submittedDate"));
                document.append("submittedDate", dateStart);
                docList.add(document);
            }
            JsonObject obj = new JsonObject();
            obj.put("successful",
                    new JsonObject().put("code", 200)
                            .put("message", "Created"));

            Future<Void> future = cnn.postExpense(docList, userId);
            future.onComplete(ar -> {
                if (ar.failed()) {
                    routingContext.response().setStatusCode(500).end(new JsonObject().put("response", "error happened").encodePrettily());
                } else {
                    routingContext.response().setStatusCode(500).end(obj.encodePrettily());
                }
            });
        } catch(Exception ex){
            routingContext.response().setStatusCode(500).end("error occurred");
        }




    }


    public void getReport(RoutingContext routingContext) {
        try {
            MultiMap queryParams = routingContext.queryParams();
            List<String> currencyParameters = queryParams.getAll("desiredCurrencies");
            baseCurrency = routingContext.user().principal().getString("currency");
            String rawCurrencyDate = queryParams.get("currencyDate");
            String startDate = queryParams.get("startDate");
            String endDate = queryParams.get("endDate");
            DateTimeBuilder builder = new DateTimeBuilder();
            LocalDate dateStartFormatted = builder.returnFormattedDate(startDate);
            LocalDate dateEndFormatted = builder.returnFormattedDate(endDate);
            String newString = builder.returnFormattedWithTime(rawCurrencyDate);
            newString = newString + "000000";
            ObjectId idUser = new ObjectId(routingContext.user().principal().getString("id"));
            CreateReport rep = new CreateReport(client, currencyParameters, jsonLegacyArray, baseCurrency);
            if (startDate == null || endDate == null) {
                routingContext.fail(400);
            } else {
                if (queryParams.getAll("desiredCurrencies") == null) {
                    routingContext.fail(400);
                } else {
                    if (rawCurrencyDate == null) {
                        routingContext.fail(400);
                    } else {
                        boolean check = checkParamValidation(currencyParameters);
                        if (check) {
                            HttpRequest<Buffer> request = client.get(dotenv.get("url1"), dotenv.get("url2")
                                            + newString +
                                            "/to/" +
                                            newString)
                                    .putHeader(dotenv.get("h1"), dotenv.get("h1c"))
                                    .putHeader(dotenv.get("h2"), dotenv.get("h2c"))
                                    .basicAuthentication(dotenv.get("user"), dotenv.get("pass"));
                            for (String currencyParameter : currencyParameters) {
                                request.addQueryParam("c", currencyParameter + "/" + baseCurrency);
                            }
                            request.send()
                                    .onSuccess(cxt -> {
                                        currencyData = cxt.bodyAsJsonObject();
                                        Document filter = new Document("UserId", routingContext.user().principal().getString("id"))
                                                .append("submittedDate", new Document("$gt", dateStartFormatted).append("$lt", dateEndFormatted));
                                        Future<List<Document>> future = cnn.getReport(filter, dateStartFormatted, dateEndFormatted, idUser);
                                        future.onComplete(ar -> {
                                            if (ar.failed()) {
                                                routingContext.response().setStatusCode(500).end("Error");
                                            } else {
                                                JsonArray jsonDocuments = new JsonArray();
                                                for (Document document : future.result()) {
                                                    jsonDocuments.add(document);
                                                }
                                                JsonObject obj = rep.parseDoc(jsonDocuments, currencyData, startDate, endDate, rawCurrencyDate);
                                                routingContext.response().setStatusCode(200)
                                                        .putHeader("content-type", "application/json")
                                                        .end(obj.encodePrettily());
                                            }
                                        });
                                    })
                                    .onFailure(cxt ->
                                            routingContext.fail(500)
                                    );

                        } else {
                            routingContext.fail(400);
                        }
                    }
                }
            }
        }
        catch(Exception ex){
            routingContext.response().setStatusCode(500).end("error occurred");
        }
    }

    private boolean checkParamValidation(List<String> currencyParameters) {
        for (String currencyParam : currencyParameters) {
            String targetCode = currencyParam + "/" + baseCurrency;
            if (!validLegacyCodes.contains(targetCode)) {
                return false;
            }
        }
        return true;
    }

    public void getLegacyCode(){
        client.get(dotenv.get("url3"))
        .send()
        .onSuccess(response->{
                jsonLegacyArray=response.bodyAsJsonArray();
                for (int j = 0; j < jsonLegacyArray.size(); j++) {
                    String legacyCode = jsonLegacyArray.getJsonObject(j).getString("legacyCode");
                    validLegacyCodes.add(legacyCode);
                }
            }
        )
        .onFailure( response->
            System.out.println("Error happened when making request")
        );
    }


    public void getPosts(RoutingContext routingContext) {
    
        try {
            MultiMap queryParams = routingContext.queryParams();
            String startDate = queryParams.get("startDate");
            String endDate = queryParams.get("endDate");
            DateTimeBuilder builder = new DateTimeBuilder();
            if (startDate != null && endDate != null) {
                LocalDate dateStartFormatted = builder.returnFormattedDate(startDate);
                LocalDate dateEndFormatted = builder.returnFormattedDate(endDate);
                ObjectId idUser = new ObjectId(routingContext.user().principal().getString("id"));
                Document filter = new Document();
                Future<List<Document>> future = cnn.getReport(filter, dateStartFormatted, dateEndFormatted, idUser);
                future.onComplete(ar -> {
                    if (ar.failed()) {
                        routingContext.response().setStatusCode(500).end("Error");
                    } else {
                        JsonArray jsonDocuments = new JsonArray();
                        for (Document document : future.result()) {
                            String id = document.getObjectId("_id").toString();
                            document.remove("_id");
                            document.remove("password");
                            document.append("id", id);
                            jsonDocuments.add(document);
                        }
                        routingContext.response().setStatusCode(200)
                                .putHeader("content-type", "application/json")
                                .end(jsonDocuments.encodePrettily());
                    }
                });
            } else {
                routingContext.fail(400);
            }
        }
        catch(Exception ex){
            routingContext.response().setStatusCode(500).end("error occurred");
        }
    }



}
