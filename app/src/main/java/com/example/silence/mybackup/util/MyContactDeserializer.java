package com.example.silence.mybackup.util;

import android.util.Log;

import com.example.silence.mybackup.entiry.MyContact;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.deser.std.CollectionDeserializer;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class MyContactDeserializer extends JsonDeserializer<MyContact> {
    @Override
    public MyContact deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        // 当前 token
        JsonToken token = parser.getCurrentToken();
        // 验证
        if (token != JsonToken.START_OBJECT) throw new com.example.silence.mybackup.exception.JsonProcessingException("意外的 JsonToken，期望一个 START_OBJECT 值.");
        // 保存 json 解析记录
        Deque<JsonToken> location = new ArrayDeque<JsonToken>();
        MyContact contact = new MyContact();
        // 加入开始对象的标记
        location.add(token);
        while (location.size() > 0) {
            token = parser.nextToken();
            switch (token) {
                case END_OBJECT:
                    if (location.pop() != JsonToken.START_OBJECT) throw new  com.example.silence.mybackup.exception.JsonProcessingException("意外的解析结构，在" + parser.getCurrentName());
                    break;
                case END_ARRAY:
                    if (location.pop() != JsonToken.START_ARRAY) throw new  com.example.silence.mybackup.exception.JsonProcessingException("意外的解析结构，在" + parser.getCurrentName());
                    break;
                case START_OBJECT:
                    location.push(token);
                    break;
                case START_ARRAY:
                    location.push(token);
                    break;
                case FIELD_NAME:
                    // 处理 name 属性
                    if ("name".equals(parser.getCurrentName().trim().toLowerCase()))  contact.setName(parser.nextTextValue());
                    else if("properties".equals(parser.getCurrentName().trim().toLowerCase()))
                        while ((token = parser.nextToken()) != JsonToken.END_ARRAY)
                            if (token == JsonToken.FIELD_NAME)
                                contact.addProperty(parser.getCurrentName(), parser.nextTextValue());
                    break;
            }
        }

        return contact;
    }
}
