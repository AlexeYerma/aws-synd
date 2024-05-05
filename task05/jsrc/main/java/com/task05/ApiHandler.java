package com.task05;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(lambdaName = "api_handler",
	roleName = "api_handler-role",
	isPublishVersion = false,
//	aliasName = "${lambdas_alias_name}",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
public class ApiHandler implements RequestHandler<Request, Map<String, Object>> {
	private Regions REGION = Regions.EU_CENTRAL_1;
	private String DYNAMODB_TABLE_NAME = "cmtr-aed3c045-Events-test";

	private static final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
	private DynamoDB dynamoDB = new DynamoDB(client);

	private AmazonDynamoDB amazonDynamoDB;

	public Map<String, Object> handleRequest(Request request, Context context) {
		this.initDynamoDbClient();
//		Map<String, AttributeValue> attributesMap = new HashMap<>();
//		attributesMap.put("id", new AttributeValue(String.valueOf(request.getPrincipalId())));
//		attributesMap.put("request", new AttributeValue(request.getContent().toString()));
//		amazonDynamoDB.putItem(DYNAMODB_TABLE_NAME, attributesMap);

		try {
			UUID uuid = UUID.randomUUID();
			// Assume an event contains only flat keys of type String.
			Item item = new Item();
			var principal = request.getPrincipalId();
			item.withString("id", uuid.toString());
			item.withInt("principalId", principal);
			var content =  request.getContent();
			item.withMap("body", content);
			context.getLogger().log("Content: " + content);
			item.withString("createdAt", ZonedDateTime.now( ZoneOffset.UTC ).format( DateTimeFormatter.ISO_INSTANT ));
			var result = item.asMap();
			Table table = dynamoDB.getTable(DYNAMODB_TABLE_NAME);
			PutItemOutcome outcome = table.putItem(item);
			context.getLogger().log("PutItem succeeded: " + outcome.getPutItemResult());

			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put("statusCode", 201);
			resultMap.put("event", result);
			return resultMap;

		} catch (Exception e) {
			context.getLogger().log(e.getMessage());
			throw new RuntimeException(e);
		}

//		System.out.println("Hello from lambda");
//		Map<String, Object> resultMap = new HashMap<String, Object>();
//		resultMap.put("statusCode", 200);
//		resultMap.put("body", "Hello from Lambda");
//		resultMap.put("mm", request.getContent());
//		return resultMap;
	}

	private void initDynamoDbClient() {
		this.amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
				.withRegion(REGION)
				.build();
	}

//	private static final String DYNAMODB_TABLE = "cmtr-aed3c045-Events-test";
//
//
//	public  Map<String, Object> handleRequest(Map<String,Object> inputEvent, Context context) {
//		context.getLogger().log("Input: " + inputEvent);
//
//		try {
//			UUID uuid = UUID.randomUUID();
//			// Assume an event contains only flat keys of type String.
//			Item item = new Item();
//			var principal = inputEvent.remove("principalId");
//			item.withString("id", uuid.toString());
//			item.withString("principalId", principal.toString());
//			item.withMap("body", inputEvent);
//
//			Table table = dynamoDB.getTable(DYNAMODB_TABLE);
//			PutItemOutcome outcome = table.putItem(item);
//			context.getLogger().log("PutItem succeeded: " + outcome.getPutItemResult());
//		} catch (Exception e) {
//			context.getLogger().log(e.getMessage());
//			throw new RuntimeException(e);
//		}
//
//		return inputEvent;
//	}
}
