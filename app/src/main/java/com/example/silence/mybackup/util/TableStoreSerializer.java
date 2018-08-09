package com.example.silence.mybackup.util;

import android.util.Log;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.Serializers;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;

import java.io.IOException;
import java.util.Arrays;

public class TableStoreSerializer extends JsonSerializer<TableStore>{

    @Override
    public void serialize(TableStore value, JsonGenerator json, SerializerProvider provider) throws IOException, JsonProcessingException {
        json.writeStartArray();

        JavaType type = TypeFactory.defaultInstance().constructArrayType(String[].class);
        JsonSerializer serializers = provider.findTypedValueSerializer(type, true, null);

        serializers.serialize(Arrays.copyOf(value.fields(), value.getWidth()), json, provider);
        for (TableStore.Row r : value) {
            serializers.serialize(r.toArray(),json,  provider);
        }

        json.writeEndArray();
    }
}
