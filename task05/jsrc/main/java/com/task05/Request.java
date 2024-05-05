package com.task05;

import java.util.Map;

public class Request {
    private int principalId;
    private Map<String, String> content;

    public Request(int principalId, Map<String, String> content) {
        this.principalId = principalId;
        this.content = content;
    }

    public Request() {
    }

    public int getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(int principalId) {
        this.principalId = principalId;
    }

    public Map<String, String> getContent() {
        return content;
    }

    public void setContent(Map<String, String> content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Request request = (Request) o;

        if (principalId != request.principalId) return false;
        return content != null ? content.equals(request.content) : request.content == null;
    }

    @Override
    public int hashCode() {
        int result = principalId;
        result = 31 * result + (content != null ? content.hashCode() : 0);
        return result;
    }
}
