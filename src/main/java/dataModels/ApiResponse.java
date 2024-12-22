package dataModels;

import com.google.gson.JsonElement;

public class ApiResponse {
    private final JsonElement jsonElement;
    private final int statusCode;

    public ApiResponse(JsonElement jsonElement, int statusCode) {
        this.jsonElement = jsonElement;
        this.statusCode = statusCode;
    }

    public JsonElement getJsonElement() {
        return jsonElement;
    }

    public int getStatusCode() {
        return statusCode;
    }
}

