package com.example.silence.mybackup.util;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.deser.ArrayDeserializer;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;

import java.io.IOException;
import java.util.Objects;

public class TableStoreDeserializer extends JsonDeserializer<TableStore>{
    @Override
    public TableStore deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
        DeserializerProvider provider = context.getDeserializerProvider();
        JavaType type = TypeFactory.defaultInstance().constructArrayType(Object[].class);
        JsonDeserializer deserializer =  provider.findValueDeserializer(context.getConfig(), type, null);



        return null;
    }
}
