package com.task06;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import org.apache.commons.collections.map.SingletonMap;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(lambdaName = "audit_producer",
	roleName = "audit_producer-role",
	isPublishVersion = false,
//	aliasName = "${lambdas_alias_name}",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@DynamoDbTriggerEventSource(
		targetTable = "Configuration",
		batchSize = 1
)
@DependsOn(
		name = "Configuration",
		resourceType = ResourceType.DYNAMODB_TABLE
)
@DependsOn(
		name = "Audit",
		resourceType = ResourceType.DYNAMODB_TABLE
)
public class AuditProducer implements RequestHandler<DynamodbEvent, Map<String, Object>> {
	private Regions REGION = Regions.EU_CENTRAL_1;
	private String DYNAMODB_TABLE_NAME = "cmtr-aed3c045-Audit-test";

	private static final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
	private DynamoDB dynamoDB = new DynamoDB(client);

	public Map<String, Object> handleRequest(DynamodbEvent request, Context context) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		for (DynamodbEvent.DynamodbStreamRecord record : request.getRecords()) {
			int i = 0;
			i++;
			UUID uuid = UUID.randomUUID();
			Item item = new Item();
			String itemKey = record.getDynamodb().getNewImage().get("key").getS();
			item.withString("id", uuid.toString());
			item.withString("itemKey", itemKey);
			item.withString("modificationTime", ZonedDateTime.now( ZoneOffset.UTC ).format( DateTimeFormatter.ISO_INSTANT ));

			if ("INSERT".equals(record.getEventName())) {
				String json = "{\"key\": \"" + itemKey + "\", \"value\": " + record.getDynamodb().getNewImage().get("value").getN()  +"}";
				item.withJSON("newValue", json);
			} else {
				item.withString("updatedAttribute", "value");
				item.withString("oldValue", record.getDynamodb().getOldImage().get("value").getN());
				item.withString("newValue", record.getDynamodb().getNewImage().get("value").getN());
			}
			Table table = dynamoDB.getTable(DYNAMODB_TABLE_NAME);
			PutItemOutcome outcome = table.putItem(item);
			context.getLogger().log("PutItem succeeded: " + outcome.getPutItemResult());

			resultMap.put("statusCode", 200);
			resultMap.put("record" + i, item.asMap());
		}

		return resultMap;
	}
}
