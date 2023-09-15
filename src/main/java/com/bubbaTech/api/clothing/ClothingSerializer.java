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
    }

    public String removeDescriptors(String string) {
        //Removes parenthesis
        string = string.replaceAll("\\(.+\\)", "");

        //Removes after symbols
        string = string.replaceAll("( - | \\| |\\*).+", "");

        //Removes color descriptors
        string = string.replaceAll(" [Ii]n .+", "");
        return string;
    }
}