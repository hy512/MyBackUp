package com.example.silence.mybackup.server;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ShortMessageServer {
    private ContentResolver resolver;

    public ShortMessageServer(ContentResolver resolver) {
        this.resolver = resolver;
    }

//    public void load() {
//        Map<String, com.example.silence.mymessage.entiry.PhoneSMS> sms = new HashMap<String, com.example.silence.mymessage.entiry.PhoneSMS>();
//
//        Uri uri = Uri.parse("content://sms/");
//        Cursor cursor = resolver.query(uri,
//                new String[]{"_id", "address", "date", "type", "body"},
//                null, null, null);
//
//        while (cursor.moveToNext()) {
//            String phone = cursor.getString(cursor.getColumnIndex("address"));
//            com.example.silence.mymessage.entiry.PhoneSMS sm;
//            if (!sms.containsKey(phone)) {
//                sm = new com.example.silence.mymessage.entiry.PhoneSMS();
//                sm.setPhone(phone);
//                sms.put(phone, sm);
//            } else {
//                sm = sms.get(phone);
//            }
//
//            sm.addSm(
//                    cursor.getLong(2),
//                    cursor.getInt(3),
//                    cursor.getString(4)
//            );
//        }
//
//        StringBuilder builder = new StringBuilder();
//        for (Map.Entry<String, com.example.silence.mymessage.entiry.PhoneSMS> entry: sms.entrySet()) {
//            builder.append(entry.getValue().toString().concat("\n\n\n"));
//        }
//    }

    public void load() {
        Cursor cursor = resolver.query(
                Uri.parse("content://mms"),
                new String[] {
                        Telephony.Mms._ID,
                        Telephony.TextBasedSmsColumns.DATE,
                        Telephony.TextBasedSmsColumns.PERSON,
                        Telephony.TextBasedSmsColumns.TYPE,
                        Telephony.TextBasedSmsColumns.ADDRESS,
                        Telephony.TextBasedSmsColumns.STATUS,
                        Telephony.TextBasedSmsColumns.READ,
                        Telephony.TextBasedSmsColumns.BODY
                }, null, null, null
        );
        while(cursor.moveToNext()){

        }
    }
}
