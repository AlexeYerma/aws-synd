package com.task08;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.*;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import org.apache.commons.lang3.StringUtils;


@DependsOn(
		name = "sdk-layer",
		resourceType = ResourceType.LAMBDA_LAYER
)
@LambdaHandler(
		lambdaName = "api_handler",
		roleName = "api_handler-role",
		layers = {"sdk-layer"},
		runtime = DeploymentRuntime.JAVA11,
		architecture = Architecture.ARM64,
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
//	isPublishVersion = true,
//	aliasName = "${lambdas_alias_name}",
)
@LambdaLayer(
		layerName = "sdk-layer",
		libraries = {"lib/commons-lang3-3.14.0.jar", "lib/gson-2.10.1.jar"},
		runtime = DeploymentRuntime.JAVA11,
		architectures = {Architecture.ARM64},
		artifactExtension = ArtifactExtension.ZIP
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
public class ApiHandler implements RequestHandler<Object, String> {

	private static final HttpClient httpClient = HttpClient.newHttpClient();
	private static final String OPEN_METEO_API_URL = "https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&hourly=temperature_2m"; // Replace with actual Open-Meteo API URL

	public String handleRequest(Object request, Context context) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			String mock = StringUtils.EMPTY;
			System.out.println("-F" + mock);
			HttpRequest httpRequest = HttpRequest.newBuilder()
					.GET()
					.uri(URI.create(OPEN_METEO_API_URL))
					.build();

			HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());


			return httpResponse.body().replaceAll("\\\"", "\"");
			// For simplicity, we are putting the en"tire response into the body. In a real application, you'd probably parse the response and format it nicely.
//			resultMap.put("statusCode", httpResponse.statusCode());
//			resultMap.put("body", httpResponse.body());
		} catch (IOException | InterruptedException e) {
			resultMap.put("statusCode", 500);
			resultMap.put("body", "Error while retrieving the weather forecast: " + e.getMessage());
		}
		return "resultMap";
	}
}
