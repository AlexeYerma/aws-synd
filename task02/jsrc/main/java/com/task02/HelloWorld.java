package com.task02;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
@LambdaHandler(lambdaName = "hello_world",
	roleName = "hello_world-role",
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
public class HelloWorld implements RequestHandler<Object, String> {

	public String handleRequest(Object request, Context context) {
		System.out.println("Hello from lambda");
		Map<String, Object> resultMap = new LinkedHashMap<>();
//		resultMap.put("body", "{\"statusCode\": 200, \"message\": \"Hello from Lambda\"}");
//		resultMap.put("headers", "{\"contentType\": application/json}");
//		resultMap.put("message", "Hello from Lambda");
//		resultMap.put("statusCode", 200);
		return "{\"statusCode\": 200, \"message\": \"Hello from Lambda\"}";
	}
}
