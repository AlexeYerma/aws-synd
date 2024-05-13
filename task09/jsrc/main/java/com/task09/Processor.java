package com.task09;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.*;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.syndicate.deployment.model.TracingMode.Active;

@LambdaHandler(
		lambdaName = "processor",
		roleName = "processor-role",
		layers = {"sdk-layer"},
		isPublishVersion = false,
		runtime = DeploymentRuntime.JAVA11,
		architecture = Architecture.ARM64,
		tracingMode = Active,
//	aliasName = "${lambdas_alias_name}",
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
@LambdaLayer(
		layerName = "sdk-layer",
		libraries = {"lib/commons-lang3-3.14.0.jar", "lib/gson-2.10.1.jar"},
		runtime = DeploymentRuntime.JAVA11,
		architectures = {Architecture.ARM64},
		artifactExtension = ArtifactExtension.ZIP
)
@DependsOn(
		name = "Weather",
		resourceType = ResourceType.DYNAMODB_TABLE
)
public class Processor implements RequestHandler<Object, Map<String, Object>> {
	private Regions REGION = Regions.EU_CENTRAL_1;
	private String DYNAMODB_TABLE_NAME = "cmtr-aed3c045-Weather-test";
	private static final HttpClient httpClient = HttpClient.newHttpClient();
	private static final String OPEN_METEO_API_URL = "https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&hourly=temperature_2m"; // Replace with actual Open-Meteo API URL

	private static final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
	private DynamoDB dynamoDB = new DynamoDB(client);

	public Map<String, Object> handleRequest(Object request, Context context) {
		try {
			UUID uuid = UUID.randomUUID();
			Gson gson = new Gson();
			Item item = new Item();
			item.withString("id", uuid.toString());
			System.out.println("Some test " + StringUtils.EMPTY);
			HttpRequest httpRequest = HttpRequest.newBuilder()
					.GET()
					.uri(URI.create(OPEN_METEO_API_URL))
					.build();

			HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

			item.withJSON("forecast", httpResponse.body().replaceAll("\\\"", "\""));

			Table table = dynamoDB.getTable(DYNAMODB_TABLE_NAME);
			PutItemOutcome outcome = table.putItem(item);
			context.getLogger().log("PutItem succeeded: " + outcome.getPutItemResult());

			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put("statusCode", 201);
			resultMap.put("event", httpResponse.body());
			return resultMap;
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException();
		}

	}
}
