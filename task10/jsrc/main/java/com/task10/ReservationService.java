package com.task10;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.google.gson.Gson;
import com.task10.dto.ReservationDTO;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

public class ReservationService {

    private static final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
    private DynamoDB dynamoDB = new DynamoDB(client);

    public Map<String, Object> handleListReservationsRequest(String tableName) {
        Table table = dynamoDB.getTable(tableName);
        ItemCollection<ScanOutcome> items = table.scan();
        var result = new LinkedList<Map<String, Object>>();

        Iterator<Item> iterator = items.iterator();
        while (iterator.hasNext()) {
            Item item = iterator.next();
            System.out.println("Retrieved tables item:" + item.toJSONPretty());

            result.add(item.asMap());
        }

        return Map.of("reservations", result);
    }

    public Map<String, Object>  handleCreateReservationRequest(String body, String tableName) {
        var request = new Gson().fromJson(body, ReservationDTO.class);
        System.out.println("ReservationPostRequest = " + request);
        UUID uuid = UUID.randomUUID();
        Item item = new Item();
        item.withString("id", uuid.toString());
        item.withInt("tableNumber", request.getTableNumber());
        item.withString("clientName", request.getClientName());
        item.withString("phoneNumber", request.getPhoneNumber());
        item.withString("date", request.getDate());
        item.withString("slotTimeStart", request.getSlotTimeStart());
        item.withString("slotTimeEnd", request.getSlotTimeEnd());

        Table table = dynamoDB.getTable(tableName);
        PutItemOutcome outcome = table.putItem(item);
        System.out.println("PutItem succeeded: " + outcome.getPutItemResult());

        return Map.of("reservationId", uuid.toString());
    }
}
