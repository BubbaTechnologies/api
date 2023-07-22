package com.bubbaTech.api.clothing;

import com.bubbaTech.api.ApiApplication;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ClothingSerializer extends JsonSerializer<ClothingDTO> {
    @Override
    public void serialize(ClothingDTO clothingDTO, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        List<String> imageUrls = new ArrayList<>();
        try {
            //Builds url
            URL url = new URL("https://" + ApiApplication.imageProcessingAddr + "/images?clothingId=" + clothingDTO.getId());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoInput(true);

            if (connection.getResponseCode() == 200) {
                JSONObject jsonResponse = ClothingService.getConnectionResponse(connection);
                JSONArray imageUrlArray = (JSONArray) jsonResponse.get("imageUrls");
                for (Object imageUrl : imageUrlArray)
                    imageUrls.add((String) imageUrl);
            } else {
                throw new Exception("Could not connect to image processing server at: " + ApiApplication.imageProcessingAddr);
            }
        } catch (Exception e) {
            e.printStackTrace();
            imageUrls = clothingDTO.getImageURL();
        }

        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", clothingDTO.getId());
        jsonGenerator.writeStringField("name", clothingDTO.getName());
        jsonGenerator.writeArrayFieldStart("imageURL");
        for (String url : imageUrls) {
            jsonGenerator.writeString(url);
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeStringField("productURL", clothingDTO.getProductURL());
        jsonGenerator.writeObjectField("store", clothingDTO.getStore());
        jsonGenerator.writeStringField("type", clothingDTO.getType().toString());
        jsonGenerator.writeStringField("gender", clothingDTO.getGender().toString());
        jsonGenerator.writeStringField("date", clothingDTO.getDate().toString());
    }
}
