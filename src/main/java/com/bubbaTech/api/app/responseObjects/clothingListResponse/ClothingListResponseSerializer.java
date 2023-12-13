package com.bubbaTech.api.app.responseObjects.clothingListResponse;

import com.bubbaTech.api.clothing.ClothingDTO;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class ClothingListResponseSerializer extends JsonSerializer<ClothingListResponse> {
    @Override
    public void serialize(ClothingListResponse clothingListResponse, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeFieldName("clothingList");
        jsonGenerator.writeStartArray();
        for (ClothingDTO clothingDTO : clothingListResponse.getClothingList()) {
            serializerProvider.defaultSerializeValue(clothingDTO, jsonGenerator);
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeFieldName("totalPages");
        jsonGenerator.writeNumber(clothingListResponse.getTotalPageCount());
        jsonGenerator.writeEndObject();
    }
}
