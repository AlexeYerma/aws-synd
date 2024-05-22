package com.task11;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.google.gson.Gson;
import com.task11.dto.ReservationDTO;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ReservationService {
    TableService tableService = new TableService();

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

    public Map<String, Object>  handleCreateReservationRequest(String body, String rName, String tName) {
        var request = new Gson().fromJson(body, ReservationDTO.class);
        System.out.println("ReservationPostRequest = " + request);
        UUID uuid = UUID.randomUUID();
        checkTableExist(request.getTableNumber(), tName);
        checkOverlapping(request, rName);
        Item item = new Item();
        item.withString("id", uuid.toString());
        item.withInt("tableNumber", request.getTableNumber());
        item.withString("clientName", request.getClientName());
        item.withString("phoneNumber", request.getPhoneNumber());
        item.withString("date", request.getDate());
        item.withString("slotTimeStart", request.getSlotTimeStart());
        item.withString("slotTimeEnd", request.getSlotTimeEnd());

        Table table = dynamoDB.getTable(rName);
        PutItemOutcome outcome = table.putItem(item);
        System.out.println("PutItem succeeded: " + outcome.getPutItemResult());

        return Map.of("reservationId", uuid.toString());
    }

    private void checkTableExist(int tableNumber, String tName) {
        System.out.println("Checking reservation tale");
        ((LinkedList<Map<String, Object>>) tableService.handleListTablesRequest(tName).get("tables"))
                .stream()
                .peek(System.out::println)
                .filter(map -> ((BigDecimal) map.get("number")).intValue() == tableNumber)
                .findAny()
                .orElseThrow(() -> new RuntimeException("Table does not exist"));
    }

    private void checkOverlapping(ReservationDTO request, String rName) {
        System.out.println("Checking overlapping");
        LocalTime checkStart = LocalTime.parse(request.getSlotTimeStart());
        LocalTime checkEnd = LocalTime.parse(request.getSlotTimeEnd());
        System.out.println("New reservation: " + request);
        ((LinkedList<Map<String, Object>>) handleListReservationsRequest(rName).get("reservations"))
                .stream()
                .peek(map -> {
                    System.out.println(map.get("tableNumber") + " " + map.get("tableNumber").getClass());
                    System.out.println(map.get("date") + " " + map.get("date").getClass());
                    System.out.println(map.get("slotTimeStart") + " " + map.get("date").getClass());
                    System.out.println(map.get("slotTimeEnd") + " " + map.get("date").getClass());
                })
                .filter(map -> ((BigDecimal) map.get("tableNumber")).intValue() == (request.getTableNumber()))
                .filter(map -> map.get("date").equals(request.getDate()))
                .peek(System.out::println)
                .forEach(x -> {
                    System.out.println(x);
                    LocalTime slotStart = LocalTime.parse(x.get("slotTimeStart").toString());
                    LocalTime slotEnd = LocalTime.parse(x.get("slotTimeEnd").toString());
                    if(checkEnd.isAfter(slotStart) && checkStart.isBefore(slotEnd)) {
                        System.out.println("Times are overlapping");
                        throw new RuntimeException();
                    }
                });

    }
}
