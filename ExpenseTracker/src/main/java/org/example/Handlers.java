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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Handlers {
    private MongoDbConnection cnn;
    private WebClient client;
    private Vertx vertx;
    private String baseCurrency;
    private JsonArray jsonLegacyArray;
    private JsonObject currencyData;
    public Handlers(MongoDbConnection _cnn,WebClient _client,Vertx _vertx)
    {
        cnn=_cnn;
        client=_client;
        vertx=_vertx;
        getLegacyCode();
    }
    public void createUser(RoutingContext routingContext) {
        JsonObject obj= routingContext.getBodyAsJson();
        Document document = new Document("name", obj.getString("name"))
                .append("password", obj.getString("password"))
                .append("currency", obj.getString("currency"));
        cnn.InsertUser(document, new CheckUserCallBack() {
            @Override
            public void onResult(boolean userCreated, Throwable error) {
                if(error!=null){
                    routingContext.response().end(error.getMessage());
                }
                else{
                    if (userCreated) {
                        routingContext.response().setStatusCode(201).end(new JsonObject().put("message","user created").encodePrettily());
                    } else {
                        routingContext.response().end(new JsonObject().put("message","user could not created").encodePrettily());
                    }
                }
            }
        });
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
        DateTimeBuilder builder = new DateTimeBuilder();
        JsonArray cntxType=routingContext.body().asJsonArray();
        List<Document> docList= new ArrayList<>();
            for(int i=0;i<cntxType.size();i++){
                Document document= new Document();
                JsonObject obj= cntxType.getJsonObject(i);
                Set<String> fieldSet=obj.getJsonObject("expenseType").fieldNames();
                List<String> fieldNames= new ArrayList<>();
                for (String fieldName : fieldSet) {
                    fieldNames.add(fieldName);
                }
                Document fieldDoc=new Document();
                for(int j=0;j<fieldNames.size();j++){
                    fieldDoc
                            .append(fieldNames.get(j),obj.getJsonObject("expenseType").getDouble(fieldNames.get(j)));
                }
                document.append("expenseType",fieldDoc);
                document.append("UserId",routingContext.user().principal().getString("id"));
                LocalDate dateStart=builder.returnFormattedDate(obj.getString("submittedDate"));
                document.append("submittedDate",dateStart);
                docList.add(document);
            }

        cnn.postExpense(docList, new PostSuccesfull() {
            @Override
            public void onResult(boolean postSucess, Throwable error) {
                JsonObject obj= new JsonObject();
                obj.put("successful",
                        new JsonObject().put("code",200)
                                .put("message","Created"));
                if(error!=null){
                    routingContext.response().setStatusCode(500).end(error.getMessage());
                }
                else{
                    if (postSucess) {
                        routingContext.response().setStatusCode(200).end(obj.encodePrettily());
                    } else {
                        routingContext.response().setStatusCode(500).end("post could not created");
                    }
                }
            }
        });

    }


    public void getReport(RoutingContext routingContext) {
        MultiMap queryParams = routingContext.queryParams();
        List<String>currencyParameters= queryParams.getAll("desiredCurrencies");
        baseCurrency=routingContext.user().principal().getString("currency");
        String rawCurrencyDate=queryParams.get("currencyDate");
        String startDate = queryParams.get("startDate");
        String endDate = queryParams.get("endDate");
        DateTimeBuilder builder = new DateTimeBuilder();
        LocalDate dateStartFormatted = builder.returnFormattedDate(startDate);
        LocalDate dateEndFormatted = builder.returnFormattedDate(endDate);
        String newString= builder.returnFormattedWithTime(rawCurrencyDate);
        newString=newString+"000000";
        CreateReport rep= new CreateReport(client,currencyParameters,jsonLegacyArray,baseCurrency);
        if(startDate==null || endDate==null){
            routingContext.fail(400);
        }
        else{
            if(queryParams.getAll("desiredCurrencies")==null){
                routingContext.fail(400);
            }
            else{
                if(rawCurrencyDate==null){
                    routingContext.fail(400);
                }
                else{
                    boolean check=checkParamValidation(currencyParameters);
                    if(check){
                        HttpRequest<Buffer> request=client.get("cartcurt"
                                        + newString +
                                        "/to/" +
                                        newString)
                                .putHeader("resource","cartcurt")
                                .putHeader("company","cartcurt")
                                .basicAuthentication("cartcurt","cartcurt");
                        for(int i=0;i<currencyParameters.size();i++){
                            request.addQueryParam("c",currencyParameters.get(i)+"/"+baseCurrency);
                        }
                        request.send()
                                .onSuccess(cxt-> {
                                    currencyData = cxt.bodyAsJsonObject();
                                    Document filter= new Document("UserId", routingContext.user().principal().getString("id"))
                                            .append("submittedDate", new Document("$gt", dateStartFormatted).append("$lt",dateEndFormatted));
                                    cnn.getReport(filter, documents -> {
                                        JsonArray jsonDocuments = new JsonArray();
                                        for (Document document : documents) {
                                            jsonDocuments.add(document);
                                        }
                                        JsonObject obj = rep.parseDoc(jsonDocuments,currencyData,startDate,endDate,rawCurrencyDate);
                                        routingContext.response().setStatusCode(200)
                                                .putHeader("content-type", "application/json")
                                                .end(obj.encodePrettily());
                                    });
                                })
                                .onFailure(cxt->
                                        routingContext.fail(500)
                                );

                    }
                    else{
                        routingContext.fail(400);
                    }
                }
            }
        }


    }

    private boolean checkParamValidation(List<String> currencyParameters) {
        boolean result=false;
        for(int i=0;i<currencyParameters.size();i++){
            for(int j=0;j<jsonLegacyArray.size();j++){
                if(jsonLegacyArray.getJsonObject(j).getString("legacyCode").equals(currencyParameters.get(i)+"/"+baseCurrency)){
                    result=true;
                    break;
                }
                else {
                    result=false;
                }
            }
            if(!result){
                break;
            }
        }
        return result;
    }

    public void getLegacyCode(){
        client.get("cartcurt.com")
                .send()
                .onSuccess(response->
                        jsonLegacyArray=response.bodyAsJsonArray()
                )
                .onFailure( response->
                    System.out.println("Error happened when making request")
                );
    }


    public void getPosts(RoutingContext routingContext) {
        MultiMap queryParams = routingContext.queryParams();
        String startDate = queryParams.get("startDate");
        String endDate = queryParams.get("endDate");
        DateTimeBuilder builder = new DateTimeBuilder();
        if(startDate !=null && endDate!=null){
        LocalDate dateStartFormatted = builder.returnFormattedDate(startDate);
        LocalDate dateEndFormatted = builder.returnFormattedDate(endDate);
        Document filter= new Document("UserId", routingContext.user().principal().getString("id"))
                .append("submittedDate", new Document("$gt", dateStartFormatted).append("$lt",dateEndFormatted));

            cnn.getExpenses(filter,documents -> {
                JsonArray jsonDocuments = new JsonArray();
                for (Document document : documents) {
                    String id=document.getObjectId("_id").toString();
                    document.remove("_id");
                    document.append("id",id);
                    jsonDocuments.add(document);
                }
                routingContext.response().setStatusCode(200)
                        .putHeader("content-type", "application/json")
                        .end(jsonDocuments.encodePrettily());
            });
        }
        else{
            routingContext.fail(400);
        }


    }



}
