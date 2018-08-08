package com.example.silence.mybackup.server;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import com.example.silence.mybackup.util.TableStore;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ShortMessageServer extends AbsBackupServer {
    private static ShortMessageServer server;

    private ShortMessageServer() {
    }


    public static BackupServer instance() throws IllegalStateException {
        if (resolver == null)
            throw new IllegalStateException(String.format("ContentResolver is null."));
        if (server == null) server = new ShortMessageServer();
        return server;
    }


    public static BackupServer instance(ContentResolver resolver) {
        Objects.requireNonNull(resolver);
        ShortMessageServer.resolver = resolver;
        return instance();
    }

  /*
    public String load() {
        Map<String, com.example.silence.mymessage.entiry.PhoneSMS> sms = new HashMap<String, com.example.silence.mymessage.entiry.PhoneSMS>();

        Uri uri = Uri.parse("content://sms/");
        Cursor cursor = resolver.query(uri,
                new String[]{"_id", "address", "date", "type", "body"},
                null, null, null);

        while (cursor.moveToNext()) {
            String phone = cursor.getString(cursor.getColumnIndex("address"));
            com.example.silence.mymessage.entiry.PhoneSMS sm;
            if (!sms.containsKey(phone)) {
                sm = new com.example.silence.mymessage.entiry.PhoneSMS();
                sm.setPhone(phone);
                sms.put(phone, sm);
            } else {
                sm = sms.get(phone);
            }

            sm.addSm(
                    cursor.getLong(2),
                    cursor.getInt(3),
                    cursor.getString(4)
            );
        }

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, com.example.silence.mymessage.entiry.PhoneSMS> entry: sms.entrySet()) {
            builder.append(entry.getValue().toString().concat("\n\n\n"));
        }
        return builder.toString();
    }
*/

    public TableStore load() {
        String[] fields = new String[]{
                Telephony.Mms._ID,
                Telephony.TextBasedSmsColumns.DATE,
                Telephony.TextBasedSmsColumns.PERSON,
                Telephony.TextBasedSmsColumns.TYPE,
                Telephony.TextBasedSmsColumns.ADDRESS,
                Telephony.TextBasedSmsColumns.STATUS,
                Telephony.TextBasedSmsColumns.READ,
                Telephony.TextBasedSmsColumns.BODY
        };
        Cursor cursor = resolver.query(Uri.parse("content://sms/"), fields, null, null, null);
        TableStore ms = new TableStore(fields);
        while (cursor.moveToNext()) {
            ms.insertRow(new Object[]{
                    cursor.getLong(cursor.getColumnIndex(ms.field(0))),
                    cursor.getLong(cursor.getColumnIndex(ms.field(1))),
                    cursor.getInt(cursor.getColumnIndex(ms.field(2))),
                    cursor.getString(cursor.getColumnIndex(ms.field(3))),
                    cursor.getInt(cursor.getColumnIndex(ms.field(4))),
                    cursor.getInt(cursor.getColumnIndex(ms.field(5))),
                    cursor.getString(cursor.getColumnIndex(ms.field(6))),
                    cursor.getString(cursor.getColumnIndex(ms.field(7)))
            });
        }
        return ms;
    }

    @Override
    public TableStore tidy() {
        return null;
    }


    @Override
    public TableStore retrieve() {
        return null;
    }

    @Override
    public void sync() {

    }

    @Override
    public void store(String path, TableStore store) throws IOException {
        File json = new File(path);
        // 确保文件存在
        if (!json.exists()) {
            File parent = json.getParentFile();
            if (!parent.exists()) parent.mkdirs();
            json.createNewFile();
        }
        mapper.writeValue(json, store);
    }
}
