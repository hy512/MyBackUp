package com.example.silence.mybackup.util;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.map.Deserializers;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.deser.ArrayDeserializer;
import org.codehaus.jackson.map.jsontype.impl.AsArrayTypeDeserializer;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class TableStoreDeserializer extends JsonDeserializer<TableStore> {
    @Override
    public TableStore deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
        if (parser == null) return null;
        DeserializerProvider provider = context.getDeserializerProvider();
        // 这个解析器会将值类型转换正确。 如 18 会被转换为 Integer 对象。
        JavaType type = TypeFactory.defaultInstance().constructArrayType(Object[].class);
        JsonDeserializer deserializer = provider.findValueDeserializer(context.getConfig(), type, null);

        // 获取内容
        Object[][] content = (Object[][]) deserializer.deserialize(parser, context);
        // 没有内容
        if (content.length < 0) return null;
        // 标题
        String[] fields = new String[content[0].length];
        System.arraycopy(content[0], 0, fields, 0, fields.length);
        TableStore store = new TableStore(fields);
        // 内容
        for (int i=1; i< content.length; i++) {
            store.insertRow(content[i]);
        }
//        for(Object[] e: content) System.out.println(Arrays.toString(e));
//        for(Object[] e: content)
//            for(Object l : e)
//                System.out.println(Objects.toString(l.getClass()));
        return store;
    }
}
