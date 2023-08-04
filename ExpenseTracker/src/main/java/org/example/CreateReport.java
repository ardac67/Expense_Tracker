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

    // Constructor to initialize the CreateReport object with required data.
    public CreateReport(WebClient client, List<String> currParams, JsonArray jsonLegacy, String baseCurr) {
        this.client = client;
        this.currencyParameters = currParams;
        this.jsonLegacy = jsonLegacy;
        this.baseCurr = baseCurr;
    }

    // Method to parse the given JSON document and create a report.
    //currencData stores currency exchange values
    //jsonDoc returns raw values of the expenses
    public JsonObject parseDoc(JsonArray jsonDoc, JsonObject currencyData, String sDate, String eDate, String cDate) {
        try {
            // Initialize a HashMap to store the calculated values for each field.
            HashMap<String, Double> pairs = new HashMap<>();

            // Extract the user ID from the JSON document.
            String userId = jsonDoc.getJsonObject(0).getString("_id");

            // Get currency exchange rates and store them in a JsonObject.
            JsonObject currencies = new JsonObject();
            for (String parameter : currencyParameters) {
                for (int j = 0; j < currencyData.size(); j++) {
                    currencies.put(
                        parameter + "/" + baseCurr,
                        currencyData
                            .getJsonArray(parameter + "/" + baseCurr)//mapping parameter and basecurrencies for legacycodes
                            .getJsonObject(0)
                            .getDouble("c")//getting currency closed values
                    );
                }
            }

            // Get all the field names from the Expenses object in the JSON document.
            List<String> fieldNames = new ArrayList<>();
            Set<String> fieldSet = jsonDoc.getJsonObject(0).getJsonObject("Expenses").fieldNames();
            fieldSet.remove("UserId");
            fieldSet.remove("submittedDate");
            fieldNames.addAll(fieldSet);

            // Calculate the sum of each field's data and store it in the pairs HashMap.
            double myData = 0;
            double totalData = 0;
            for (String name : fieldNames) {
                for (int j = 0; j < jsonDoc.size(); j++) {
                    myData += jsonDoc.getJsonObject(j).getJsonObject("Expenses").getDouble(name);//by expense calculation
                    totalData += jsonDoc.getJsonObject(j).getJsonObject("Expenses").getDouble(name);//total expense value calculation
                }
                pairs.put(name, myData);
                myData = 0;
            }
            pairs.put("Total", totalData);

            // Calculate the base values using the displayDoubleWithPrecision method.
            JsonObject baseValues = new JsonObject();
            for (String name : fieldNames) {
                baseValues.put(name, displayDoubleWithPrecision(pairs.get(name)));
            }
            baseValues.put("Total", displayDoubleWithPrecision(pairs.get("Total")));

            // Map currency calculations for each currency parameter and store them in a JsonObject.
            JsonObject completeMap = new JsonObject();
            for (String currencyParameter : currencyParameters) {
                JsonObject innerMap = new JsonObject();
                double curr = currencies.getDouble(currencyParameter + "/" + baseCurr);
                double total = 0;
                for (String fieldName : fieldNames) {
                    double calculation = pairs.get(fieldName) / curr;
                    innerMap.put(fieldName, displayDoubleWithPrecision(calculation));
                    total += calculation;
                }
                innerMap.put("Total", displayDoubleWithPrecision(total));
                completeMap.put(currencyParameter, innerMap);
            }

            // Create the final report JsonObject and put all the calculated data in it.
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
        } catch (Exception ex) {
            // If there's an exception, print the error message and return an error response JsonObject.
            System.out.println(ex.getMessage());
            return new JsonObject().put("response", "error");
        }
    }

    // Helper method to display a double value with a specified precision.
    public double displayDoubleWithPrecision(double value) {
        int precision = 2;
        double factor = Math.pow(10, precision);
        return Math.round(value * factor) / factor;
    }
}
