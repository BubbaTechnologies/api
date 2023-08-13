package com.bubbaTech.api.clothing;

import com.bubbaTech.api.app.AppController;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

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
        List<String> imageUrls = new ArrayList<>();
        try {
            //Builds url
            URL url = new URL("http://" + imageProcessingAddr + "/images?clothingId=" + clothingDTO.getId());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoInput(true);

            if (connection.getResponseCode() == 200) {
                JSONObject jsonResponse = ClothingService.getConnectionResponse(connection);
                JSONArray imageUrlArray = (JSONArray) jsonResponse.get("imageUrls");
                for (Object imageUrl : imageUrlArray){
                    imageUrls.add(linkTo(AppController.class)
                            .slash(imageUrl)
                            .toUriComponentsBuilder().scheme("https").toUriString());
                }
            } else {
                throw new Exception("Could not connect to image processing server at: " + imageProcessingAddr);
            }
        } catch (Exception e) {
            e.printStackTrace();
            imageUrls = clothingDTO.getImageURL();
        }

        jsonGenerator.writeNumberField("id", clothingDTO.getId());
        jsonGenerator.writeStringField("name", clothingDTO.getName());
        jsonGenerator.writeArrayFieldStart("imageURL");
        for (String url : imageUrls) {
            jsonGenerator.writeString(url);
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeStringField("productURL", clothingDTO.getProductURL());
        serializerProvider.defaultSerializeField("store", clothingDTO.getStore(), jsonGenerator);
        jsonGenerator.writeStringField("type", clothingDTO.getType().toString());
        jsonGenerator.writeStringField("gender", clothingDTO.getGender().toString());
        jsonGenerator.writeStringField("date", clothingDTO.getDate().toString());
    }
}
