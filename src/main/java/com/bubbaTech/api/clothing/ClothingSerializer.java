package com.bubbaTech.api.clothing;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;


public class ClothingSerializer extends StdSerializer<ClothingDTO> {

    @Value("${system.image_processing_addr}")
    private String imageProcessingAddr;

    public ClothingSerializer(){
        this(null);
    }

    public ClothingSerializer(final Class<ClothingDTO> item){
        super(item);
    }

    @Override
    public final void serialize(ClothingDTO clothingDTO, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", clothingDTO.getId());
        //Processes name
        jsonGenerator.writeStringField("name", removeDescriptors(clothingDTO.getName()));
        jsonGenerator.writeArrayFieldStart("imageURL");
        for (String url : clothingDTO.getImageURL()) {
            jsonGenerator.writeString(url);
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeStringField("productURL", clothingDTO.getProductURL());
        serializerProvider.defaultSerializeField("store", clothingDTO.getStore(), jsonGenerator);
        jsonGenerator.writeStringField("type", clothingDTO.getType().toString());
        jsonGenerator.writeStringField("gender", clothingDTO.getGender().toString());
        jsonGenerator.writeStringField("date", clothingDTO.getDate().toString());
        jsonGenerator.writeEndObject();
    }

    public String removeDescriptors(String string) {
        //Removes parenthesis
        string = string.replaceAll("\\(.+\\)", "");

        //Removes after symbols
        string = string.replaceAll("( - | \\| |\\*).+", "");

        //Removes color descriptors
        string = string.replaceAll(" [Ii]n .+", "");
        return capitalizeTitle(string);
    }

    private String capitalizeTitle(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true; // Flag to indicate whether the next character should be capitalized

        for (char c : str.toCharArray()) {
            if (Character.isWhitespace(c) || c == '-') {
                // If the character is whitespace or hyphen, set the flag to true
                capitalizeNext = true;
                result.append(c);
            } else if (capitalizeNext) {
                // If the flag is true, capitalize the character and reset the flag
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                // If the flag is false, keep the character as it is
                result.append(Character.toLowerCase(c));
            }
        }

        return result.toString();
    }
}