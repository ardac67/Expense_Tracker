package org.example;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import java.util.*;

public class CreateReport {
    private WebClient client;
    private List<String> currencyParameters;
    private JsonArray jsonLegacy;
    private String baseCurr;

    public CreateReport(WebClient _client,List<String> currParams,JsonArray _jsonLegacy,String _baseCurr){
        client=_client;
        currencyParameters=currParams;
        jsonLegacy=_jsonLegacy;
        baseCurr=_baseCurr;
    }
    public JsonObject parseDoc(JsonArray jsonDoc,JsonObject currencyData,String sDate,String eDate,String cDate){
        HashMap<String ,Double> pairs= new HashMap<>();
        String userId=jsonDoc.getJsonObject(0).getString("UserId");

        //getting currencies
        JsonObject currencies= new JsonObject();
        for(int i=0;i<currencyParameters.size();i++){
            for(int j=0;j<currencyData.size();j++){
                currencies.put(currencyParameters.get(i)+"/"+baseCurr
                        ,currencyData
                                .getJsonArray(currencyParameters.get(i)+"/"+baseCurr)
                                .getJsonObject(0)
                                .getDouble("c")
                        );
            }
        }
        //getting fieldnames
        List<String> fieldNames= new ArrayList<>();
        Set<String> fieldSet = jsonDoc.getJsonObject(0).getJsonObject("expenseType").fieldNames();
        for (String fieldName : fieldSet) {
            fieldNames.add(fieldName);
        }

        //getting base data
        double myData=0;
        double totalData=0;
        for(int i=0;i<fieldNames.size();i++){
            for(int j=0;j<jsonDoc.size();j++){
                myData=myData+jsonDoc.getJsonObject(j).getJsonObject("expenseType").getDouble(fieldNames.get(i));
                totalData=totalData+jsonDoc.getJsonObject(j).getJsonObject("expenseType").getDouble(fieldNames.get(i));
            }
            pairs.put(fieldNames.get(i),myData);
            myData=0;
        }
        pairs.put("Total",totalData);

        JsonObject baseValues= new JsonObject();
        for(int i=0;i<fieldNames.size();i++){
            baseValues.put(fieldNames.get(i),displayDoubleWithPrecision(pairs.get(fieldNames.get(i))));
        }
        baseValues.put("Total",displayDoubleWithPrecision(pairs.get("Total")));

        //mapping currency calculations
        JsonObject completeMap= new JsonObject();
        for (String currencyParameter : currencyParameters) {
            JsonObject innerMap = new JsonObject();
            double curr = currencies.getDouble(currencyParameter + "/" + baseCurr);
            double total = 0;
            for (String fieldName : fieldNames) {
                double calculation = pairs.get(fieldName) / curr;
                innerMap.put(fieldName, displayDoubleWithPrecision(calculation));
                total = total + calculation;
            }
            innerMap.put("Total", displayDoubleWithPrecision(total));
            completeMap.put(currencyParameter, innerMap);
        }

        //putting all together
        JsonObject report= new JsonObject();
        report.put("UserId",userId)
                .put("BaseCurrency",baseCurr)
                .put("StartDate",sDate)
                .put("EndDate",eDate)
                .put("CurrencyDate",cDate)
                .put("Currency",currencies)
                .put(baseCurr,baseValues)
                .put("OtherCurrencies",completeMap);

        return report;
    }
    public double displayDoubleWithPrecision(double value) {
        int precision=2;
        double factor = Math.pow(10, precision);
        double roundedValue = Math.round(value * factor) / factor;
        return roundedValue;
    }

}

