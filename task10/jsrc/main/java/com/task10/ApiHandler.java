package com.task10;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;

import java.util.HashMap;
import java.util.Map;

@LambdaHandler(lambdaName = "api_handler",
	roleName = "api_handler-role",
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@DependsOn(
		name = "Reservations",
		resourceType = ResourceType.DYNAMODB_TABLE
)
@DependsOn(
		name = "Tables",
		resourceType = ResourceType.DYNAMODB_TABLE
)
public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
	private AuthService authService = new AuthService();
	private TableService tableService = new TableService();
	private ReservationService reservationService = new ReservationService();

	public APIGatewayProxyResponseEvent  handleRequest(APIGatewayProxyRequestEvent request, Context context) {
		System.out.println("Lambda started");
		String poolName = context.getFunctionName().replace("api_handler", "simple-booking-userpool");
		String tablesName = context.getFunctionName().replace("api_handler", "Tables");
		String reservationName = context.getFunctionName().replace("api_handler", "Reservations");
		var pathParameters = request.getPathParameters();
		var path = request.getPath();
		var httpMethod = request.getHttpMethod();

		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("statusCode", 400);

		try {

			System.out.println("Before case");
			Map<String, Object> result = null;
			if (path.startsWith("/tables")
			&& pathParameters != null
					&& pathParameters.get("id") != null) {
				var tableId = Long.valueOf(request.getPathParameters().get("id"));
				result = tableService.getTable(tableId, tablesName);
			} else {
				switch (path) {
					case "/signup":
						result = processSignup(request.getBody(), poolName);
						break;
					case "/signin":
						result = processSignin(request.getBody(), poolName);
						break;
					case "/tables":
						result = processTables(httpMethod, request.getBody(), tablesName);
						break;
					case "/reservations":
						result = processReservations(httpMethod, request.getBody(), reservationName);
						break;
					default:
						throw new UnsupportedOperationException("Operation " +
								path + " is not supported");
				}
			}
			return new APIGatewayProxyResponseEvent()
					.withStatusCode(200)
					.withBody(new Gson().toJson(result))
					.withHeaders(Map.of("Content-Type", "application/json"));
		} catch (Exception e) {
			System.out.println(e);
			resultMap.put("error", e.getMessage() + "\n" + e.getStackTrace());
			return new APIGatewayProxyResponseEvent()
					.withStatusCode(400)
					.withBody(new Gson().toJson(resultMap.toString()));
		}
	}

	private Map<String, Object> processTables(String httpMethod, String body, String name) {
		return httpMethod.equals("GET") ?
				tableService.handleListTablesRequest(name) :
				tableService.handleCreateTableRequest(body, name);
	}

	private Map<String, Object> processReservations(String httpMethod, String body, String name) {
		return httpMethod.equals("GET") ?
				reservationService.handleListReservationsRequest(name) :
				reservationService.handleCreateReservationRequest(body, name);
	}

	private Map<String, Object> processSignin(String body, String functionName) {
		System.out.println("Process signin");
		return authService.signIn(body, functionName);
	}

	private Map<String, Object> processSignup(String body, String functionName) {
		System.out.println("Process signup");
		return authService.signUp(body, functionName);
	}
}
