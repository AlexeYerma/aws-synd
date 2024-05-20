package com.task10;

public class SingInResponse {
    private String accessToken;

    public SingInResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    public SingInResponse() {
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public String toString() {
        return "SingInResponse{" +
                "accessToken='" + accessToken + '\'' +
                '}';
    }
}
