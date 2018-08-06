package com.example.silence.mybackup.entiry;

import com.example.silence.mybackup.util.MyContactDeserializer;
import com.example.silence.mybackup.util.MyContactSerializer;
import com.example.silence.mybackup.util.SimpleStore;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonSerialize(
        using = MyContactSerializer.class
)
@JsonDeserialize(
        using = MyContactDeserializer.class
)
public class MyContact {
    String name;
    // 联系人是从本机读取
    Long raw_contact_id;
    SimpleStore<String> properties;
    // 手机号码
    List<String> phones;
    // 联系人是否需要更新
    boolean changed = false;

    public MyContact() {
        this.properties = new SimpleStore<String>();
        this.phones = new ArrayList<String>();
    }

    public void setName(String name) {
//        setChanged(true);
        if (name == null || name.isEmpty())
            this.name = null;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setRaw_contact_id(Long raw_contact_id) {
        this.raw_contact_id = raw_contact_id;
    }
    public void setRaw_contact_id(long raw_contact_id) {
        this.raw_contact_id = raw_contact_id;
    }

    public Long getRaw_contact_id() {
        return raw_contact_id;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public boolean isChanged() {
        return changed;
    }

    public void addProperty(String k, String v) {
        this.properties.put(k, v);
        if ("vnd.android.cursor.item/phone_v2".equals(k)) {
            this.phones.add(v);
            if (this.name == null) this.name = v;
        }
    }

    public SimpleStore<String>.Ent getPropertie(int i) {
        return properties.get(i);
    }

    public String getPropKey(int i) {
        return properties.getKey(i);
    }

    public String getPropValue(int i) {
        return properties.getValue(i);
    }

    public SimpleStore<String> getProperties() {
        return properties;
    }

    public String getPhone() {
        if (!this.phones.isEmpty())
            return this.phones.get(0);
        return "";
    }

    public void merge(MyContact contact) {
        for (int l = contact.getProperties().size()-1; l>=0; l--) {
            addProperty(contact.getPropKey(l), contact.getPropValue(l));
        }
    }

    public List<String> getPhones() {
        phones.clear();
        for(int i=properties.size()-1; i>=0; i--) {
            if ("vnd.android.cursor.item/phone_v2".equals(getPropKey(i)))
                phones.add(getPropValue(i));
        }
        return this.phones;
    }

    @Override
    public String toString() {
        return this.name + " " + this.properties.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof MyContact) {
            MyContact contact = (MyContact) obj;
            // 只有 raw contact id 相等才相等
            return this.getRaw_contact_id() == null ? false : this.getRaw_contact_id().equals(contact.getRaw_contact_id());
        }
        return false;
    }

}
