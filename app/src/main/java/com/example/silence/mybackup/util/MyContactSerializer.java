package com.example.silence.mybackup.util;

import com.example.silence.mybackup.entiry.MyContact;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

public class MyContactSerializer extends JsonSerializer<MyContact> {
    @Override
    public void serialize(MyContact contact, JsonGenerator generator, SerializerProvider provider) throws IOException, JsonProcessingException {
        // start
        generator.writeStartObject();
        // 输出名称
        generator.writeFieldName("name");
        generator.writeString(contact.getName());
        // 输出号码
//        generator.writeFieldName("phones");
//        generator.writeObject(contact.getPhones());
        // 输出描述
        generator.writeArrayFieldStart("properties");
        for (int i = contact.getProperties().size() - 1; i >= 0; i--) {
            generator.writeStartObject();
            generator.writeStringField(contact.getPropKey(i), contact.getPropValue(i));
            generator.writeEndObject();
        }
        generator.writeEndArray();
        // end
        generator.writeEndObject();
    }
}
