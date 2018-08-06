package com.example.silence.mymessage.entiry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/*
    表示一个手机号码上的所有短信
 */
public class PhoneSMS {
    String phone;
    List<ShortMsg> sm;

    public PhoneSMS() {
        this.sm = new ArrayList<ShortMsg>();
    }
    public PhoneSMS(String phone, long date, byte type, String body){
        ShortMsg msg = new ShortMsg(date, type, body);
        this.sm = new ArrayList<ShortMsg>();
        this.sm.add(msg);
        this.phone = phone;
    }
    public PhoneSMS(String phone, long date, int type, String body) {
        this(phone, date, (byte)type, body);
    }
    // 表示一条短信
    class ShortMsg {
        Date date;
        String body;
        byte type;

        SimpleDateFormat format;

        ShortMsg() {
            this.format = new SimpleDateFormat("yyyy年 MM月 dd日");
        }
        ShortMsg(long date, byte type, String body) {
            this();
            this.setBody(body);
            this.setDate(date);
            this.setType(type);
        }
        ShortMsg(int date, int type, String body) {
            this((long)date, (byte)type, body);
        }
        public String getFormatDate() {
            return format.format(this.date);
        }
        public Date getDate() {
            return this.date;
        }

        public void setDate(long date) {
            this.date = new Date(date);
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public byte getType() {
            return type;
        }

        public void setType(byte type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return this.getFormatDate().concat("  ").concat(this.type == 1? "收": "发").concat("\n").concat(this.getBody()).concat("\n");
        }
    }

    public void addSm(ShortMsg sm) {
        this.sm.add(sm);
    }
    public void addSm(long date, int type, String body){
        this.sm.add(new ShortMsg(date, (byte)type, body));
    }
    public void set(String phone, long date, int type, String body) {
        this.setPhone(phone);
        this.addSm(date, type, body);
    }


    public List<ShortMsg> getSm() {
        return sm;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void sort() {
        Collections.sort(this.sm, (ShortMsg o1, ShortMsg o2)->{
            if (o1.getDate().before(o2.getDate()))
                return -1;
            else if (o1.getDate().after(o2.getDate())) {
                return 1;
            }
            return 0;
        });
    }

    @Override
    public String toString() {
        this.sort();

        StringBuilder builder = new StringBuilder();
        Iterator<ShortMsg> ite = this.sm.iterator();
        while (ite.hasNext()) {
            builder.append(ite.next().toString());
        }
        return this.phone.concat("\n").concat(builder.toString());
    }
}
