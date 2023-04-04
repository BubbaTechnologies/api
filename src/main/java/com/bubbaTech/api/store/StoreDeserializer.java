//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.store;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class StoreDeserializer extends StdDeserializer<StoreDTO> {
    public StoreDeserializer() {
        this(null);
    }

    public StoreDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public StoreDTO deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        String name = (node.get("name")).textValue();
        String url = (node.get("url")).textValue();
        return new StoreDTO(name, url);
    }

}
