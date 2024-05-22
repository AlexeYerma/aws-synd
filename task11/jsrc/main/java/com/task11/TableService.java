package com.task11;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.google.gson.Gson;
import com.task11.dto.TableDTO;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class TableService {
    private static final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
    private DynamoDB dynamoDB = new DynamoDB(client);

    public Map<String, Object>  handleListTablesRequest(String tableName) {
        Table table = dynamoDB.getTable(tableName);
        ItemCollection<ScanOutcome> items = table.scan();
        var result = new LinkedList<Map<String, Object>>();

        Iterator<Item> iterator = items.iterator();
        while (iterator.hasNext()) {
            Item item = iterator.next();
            System.out.println("Retrieved tables item:" + item.toJSONPretty());

//            result.add(new TableDTO(
//                    item.getInt("id"),
//                    item.getInt("number"),
//                    item.getInt("places"),
//                    item.getBoolean("isVip"),
//                    item.getInt("minOrder")
//            ));
            result.add(item.asMap());
        }

        return Map.of("tables", result);
    }

    public Map<String, Object>  handleCreateTableRequest(String body, String tableName) {
        var request = new Gson().fromJson(body, TableDTO.class);
        System.out.println("TablePostRequest = " + request);
        Item item = new Item();
        item.withInt("id", request.getId());
        item.withInt("number", request.getNumber());
        item.withInt("places", request.getPlaces());
        item.withBoolean("isVip", request.getIsVip());
        item.withInt("minOrder", request.getMinOrder());

        Table table = dynamoDB.getTable(tableName);
        PutItemOutcome outcome = table.putItem(item);
        System.out.println("PutItem succeeded: " + outcome.getPutItemResult());

        return Map.of("id", request.getId());
    }

    public Map<String, Object> getTable(Long tableId, String tableName) {
        Table table = dynamoDB.getTable(tableName);
        Item item = table.getItem("id", Long.valueOf(tableId));
        return item.asMap();
    }
}
