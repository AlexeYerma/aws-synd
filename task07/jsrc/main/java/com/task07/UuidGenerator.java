package com.task07;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.syndicate.deployment.annotations.events.RuleEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@LambdaHandler(lambdaName = "uuid_generator",
	roleName = "uuid_generator-role",
	isPublishVersion = false,
//	aliasName = "${lambdas_alias_name}",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@RuleEventSource(
		targetRule = "uuid_trigger"
)
@DependsOn(
		name = "uuid_trigger",
		resourceType = ResourceType.CLOUDWATCH_RULE
)
@DependsOn(
		name = "uuid-storage",
		resourceType = ResourceType.S3_BUCKET
)
public class UuidGenerator implements RequestHandler<Object, Map<String, Object>> {
	private Regions REGION = Regions.EU_CENTRAL_1;
	private String BUCKET_NAME = "cmtr-aed3c045-uuid-storage-test";

	public Map<String, Object> handleRequest(Object request, Context context) {
		List uuids = List.of(
				UUID.randomUUID(),
				UUID.randomUUID(),
				UUID.randomUUID(),
				UUID.randomUUID(),
				UUID.randomUUID(),
				UUID.randomUUID(),
				UUID.randomUUID(),
				UUID.randomUUID(),
				UUID.randomUUID(),
				UUID.randomUUID());

		String result = "{\"ids\":" + uuids + "}";

		System.out.println("Hello from lambda");
		String name = ZonedDateTime.now( ZoneOffset.UTC ).format( DateTimeFormatter.ISO_INSTANT );

		AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(REGION).build();

		try (InputStream inputStream = new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8))) {
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentLength((uuids.toString().getBytes(StandardCharsets.UTF_8).length));

			PutObjectRequest putRequest = new PutObjectRequest(BUCKET_NAME, name, inputStream, metadata);
			s3Client.putObject(putRequest);
		} catch (IOException ex) {
			System.out.println(ex);// Handle the exception
		}

		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("statusCode", 200);
		resultMap.put("name", name);
		return resultMap;
	}
}
