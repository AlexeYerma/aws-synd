package com.task11;

import com.google.gson.Gson;
import com.task11.dto.SignInRequest;
import com.task11.dto.SingUpRequest;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.Map;

public class AuthService {
    public Map<String, Object> signIn(String body, String poolName) {
        var request = new Gson().fromJson(body, SignInRequest.class);

        var cognitoClient = CognitoIdentityProviderClient.create();
        var userPoolId = getUserPoolId(cognitoClient, poolName);
        var clientId = getClientId(cognitoClient, userPoolId);
        try {
            var authParams = Map.of(
                    "USERNAME", request.getEmail(),
                    "PASSWORD", request.getPassword()
            );

            var authRequest = AdminInitiateAuthRequest.builder()
                    .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                    .userPoolId(userPoolId)
                    .clientId(clientId)
                    .authParameters(authParams)
                    .build();

            var authResponse = cognitoClient.adminInitiateAuth(authRequest);
            System.out.println("Auth response: " + authResponse);
            System.out.println("ID Token: " + authResponse.authenticationResult().idToken());
            System.out.println("Access Token: " + authResponse.authenticationResult().accessToken());
            System.out.println("Refresh Token: " + authResponse.authenticationResult().refreshToken());

            return Map.of("accessToken", authResponse.authenticationResult().idToken());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> signUp(String body, String poolName) {
        var request = new Gson().fromJson(body, SingUpRequest.class);
        System.out.println("UserSignupRequest = " + request);
//        String accessKey = System.getenv("AWS_ACCESS_KEY_ID");
//        String secretKey = System.getenv("AWS_SECRET_ACCESS_KEY");
//        System.out.println("AK: " + accessKey);
//        System.out.println("SK: " + secretKey);
//        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);

//        var cognitoClient = CognitoIdentityProviderClient.builder()
//                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
//                .region(Region.EU_CENTRAL_1)
//                .build();
                var cognitoClient = CognitoIdentityProviderClient.create();

        var userPoolId = getUserPoolId(cognitoClient, poolName);
        var clientId = getClientId(cognitoClient, userPoolId);
        try {
            var createUserRequest = AdminCreateUserRequest.builder()
                    .userPoolId(userPoolId)
                    .temporaryPassword(request.getPassword())
                    .username(request.getEmail())
                    .messageAction("SUPPRESS")
                    .build();

            var setUserPasswordRequest = AdminSetUserPasswordRequest.builder()
                    .userPoolId(userPoolId)
                    .username(request.getEmail())
                    .password(request.getPassword())
                    .permanent(true)
                    .build();

            var createUserResponse = cognitoClient.adminCreateUser(createUserRequest);
            var setUserPasswordResponse = cognitoClient.adminSetUserPassword(setUserPasswordRequest);
            System.out.println("createUserResponse = " + createUserResponse);
            System.out.println("setUserPasswordResponse = " + setUserPasswordResponse);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Map.of("statusCode", 200);
    }

    private String getUserPoolId(CognitoIdentityProviderClient cognitoClient, String poolName) {
        String userPoolId = "";
        try {
            var request = ListUserPoolsRequest.builder()
                    .maxResults(10)
                    .build();

            var response = cognitoClient.listUserPools(request);
            response.userPools().forEach(userpool -> {
                System.out.println("User pool " + userpool.name() + ", User ID " + userpool.id());
            });
            userPoolId = response.userPools().stream()
                    .filter(userPool -> poolName.equals(userPool.name()))
                    .findFirst()
                    .orElseThrow()
                    .id();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        System.out.println("UserPoolId = " + userPoolId);

        return userPoolId;
    }

    private String getClientId(CognitoIdentityProviderClient cognitoClient, String userPoolId) {
        String clientId = "";
        try {
            var request = ListUserPoolClientsRequest.builder()
                    .userPoolId(userPoolId)
                    .build();

            var response = cognitoClient.listUserPoolClients(request);
            response.userPoolClients().forEach(userPoolClient -> {
                System.out.println("User pool client " + userPoolClient.clientName() + ", Pool ID "
                        + userPoolClient.userPoolId() + ", Client ID " + userPoolClient.clientId());
            });

            clientId = response.userPoolClients().get(0).clientId();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("Client ID = " + clientId);

        return clientId;
    }
}
