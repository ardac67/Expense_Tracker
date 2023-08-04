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

    public CreateReport(WebClient client,List<String> currParams,JsonArray jsonLegacy,String baseCurr){
        this.client=client;
        this.currencyParameters=currParams;
        this.jsonLegacy=jsonLegacy;
        this.baseCurr=baseCurr;
    }
    public JsonObject parseDoc(JsonArray jsonDoc,JsonObject currencyData,String sDate,String eDate,String cDate){
        try {
            HashMap<String, Double> pairs = new HashMap<>();
            String userId = jsonDoc.getJsonObject(0).getString("_id");

            //getting currencies
            JsonObject currencies = new JsonObject();
            for (String parameter : currencyParameters) {
                for (int j = 0; j < currencyData.size(); j++) {
                    currencies.put(parameter + "/" + baseCurr
                            , currencyData
                                    .getJsonArray(parameter + "/" + baseCurr)
                                    .getJsonObject(0)
                                    .getDouble("c")
                    );
                }
            }
            //getting fieldnames
            List<String> fieldNames = new ArrayList<>();
            Set<String> fieldSet = jsonDoc.getJsonObject(0).getJsonObject("Expenses").fieldNames();
            fieldSet.remove("UserId");
            fieldSet.remove("submittedDate");
            fieldNames.addAll(fieldSet);

            //getting base data
            double myData = 0;
            double totalData = 0;
            for (String name : fieldNames) {
                for (int j = 0; j < jsonDoc.size(); j++) {
                    myData = myData + jsonDoc.getJsonObject(j).getJsonObject("Expenses").getDouble(name);
                    totalData = totalData + jsonDoc.getJsonObject(j).getJsonObject("Expenses").getDouble(name);
                }
                pairs.put(name, myData);
                myData = 0;
            }
            pairs.put("Total", totalData);

            JsonObject baseValues = new JsonObject();
            for (String name : fieldNames) {
                baseValues.put(name, displayDoubleWithPrecision(pairs.get(name)));
            }
            baseValues.put("Total", displayDoubleWithPrecision(pairs.get("Total")));

            //mapping currency calculations
            JsonObject completeMap = new JsonObject();
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
            JsonObject report = new JsonObject();
            report.put("UserId", userId)
                    .put("BaseCurrency", baseCurr)
                    .put("StartDate", sDate)
                    .put("EndDate", eDate)
                    .put("CurrencyDate", cDate)
                    .put("Currency", currencies)
                    .put(baseCurr, baseValues)
                    .put("OtherCurrencies", completeMap);

            return report;
        }
        catch(Exception ex){
            System.out.println(ex.getMessage());
            return new JsonObject().put("response","error");
        }
    }
    public double displayDoubleWithPrecision(double value) {
        int precision=2;
        double factor = Math.pow(10, precision);
        return Math.round(value * factor) / factor;
    }

}

