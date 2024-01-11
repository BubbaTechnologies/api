package com.bubbaTech.api.app.responseObjects.clothingListResponse;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class ActivityListResponseSerializer extends JsonSerializer<ActivityListResponse> {

    @Override
    public void serialize(ActivityListResponse activityListResponse, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeFieldName("activityList");
        jsonGenerator.writeStartArray();
        for (ActivityLikeDTO activityLikeDTO : activityListResponse.getActivityList()) {
            serializerProvider.defaultSerializeValue(activityLikeDTO, jsonGenerator);
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeFieldName("totalPages");
        jsonGenerator.writeNumber(activityListResponse.getTotalPageCount());
        jsonGenerator.writeEndObject();
    }
}