package com.bubbaTech.api.filterOptions;

import com.bubbaTech.api.clothing.ClothType;
import com.bubbaTech.api.clothing.ClothingTag;
import com.bubbaTech.api.user.Gender;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.List;

public class FilterOptionsSerializer extends JsonSerializer<FilterOptionsDTO> {
    @Override
    public void serialize(FilterOptionsDTO filterOptionsDTO, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeFieldName("genders");
        jsonGenerator.writeStartArray();
        for (Gender gender : filterOptionsDTO.getGenders()) {
            jsonGenerator.writeString(gender.getStringValue());
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeFieldName("types");
        jsonGenerator.writeStartArray();
        for (List<ClothType> typeList : filterOptionsDTO.getTypes()) {
            jsonGenerator.writeStartArray();
            for (ClothType type : typeList) {
                if (type != ClothType.OTHER) {
                    jsonGenerator.writeString(type.getStringValue());
                }
            }
            jsonGenerator.writeEndArray();
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeFieldName("tags");
        jsonGenerator.writeStartObject();
        for (ClothType type : filterOptionsDTO.getTags().keySet()) {
            jsonGenerator.writeFieldName(type.getStringValue());
            jsonGenerator.writeStartArray();
            for (ClothingTag tag : filterOptionsDTO.getTags().get(type)) {
                jsonGenerator.writeString(tag.getStringValue());
            }
            jsonGenerator.writeEndArray();
        }
        jsonGenerator.writeEndObject();
        jsonGenerator.writeEndObject();
    }
}
