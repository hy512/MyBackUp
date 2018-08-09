package com.example.silence.mybackup.server;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.example.silence.mybackup.util.TableStore;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ShortMessageServer extends AbsBackupServer {
    public static int READ_SMS_REQUEST_CODE = 100;



    public ShortMessageServer(Activity activity) {
        contentUri = Uri.parse("content://sms/");
//        authority = "sms";
        authority = "telephony";
        this.context = activity;
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
        ContentResolver resolver = context.getContentResolver();

        String[] fields = new String[]{
                Telephony.Sms._ID,
                Telephony.Sms.DATE,
                Telephony.Sms.PERSON,
                Telephony.Sms.TYPE,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.STATUS,
                Telephony.Sms.READ,
                Telephony.Sms.BODY
        };
        Cursor cursor = resolver.query(contentUri, fields, null, null, null);
        TableStore ms = new TableStore(fields);
        while (cursor.moveToNext()) {
            ms.insertRow(new Object[]{
                    cursor.getLong(cursor.getColumnIndex(ms.field(0))),
                    cursor.getLong(cursor.getColumnIndex(ms.field(1))),
                    cursor.getString(cursor.getColumnIndex(ms.field(2))),
                    cursor.getInt(cursor.getColumnIndex(ms.field(3))),
                    cursor.getString(cursor.getColumnIndex(ms.field(4))),
                    cursor.getInt(cursor.getColumnIndex(ms.field(5))),
                    cursor.getInt(cursor.getColumnIndex(ms.field(6))),
                    cursor.getString(cursor.getColumnIndex(ms.field(7)))
            });
        }

        return ms;
    }

    @Override
    public TableStore retrieve(String path) throws IOException {
        TableStore store = super.retrieve(path);
        store.insertColumnOfNull(0, Telephony.Mms._ID);
        return store;
    }

    @Override
    public void store(String path, TableStore store) throws IOException {
        store.removeColumn(Telephony.Mms._ID);
        super.store(path, store);
    }

    @Override
    public TableStore tidy(TableStore store) {
        return null;
    }


    @Override
    public boolean sync(TableStore store)  {
        try {
            TableStore local = load();
            // 交集
            TableStore intersection = store.clone();
            intersection.retainAll(local);
            // 新内容关于本地内容的补集, 不被需要
            TableStore complementary = local;
            complementary.removeAll(intersection);
            local = null;
            // 本地内容关于新内容的相对补集, 需要添加
            store.removeAll(intersection);

            // 插入
            batchInsert(store);
            // 删除
            batchDelete(complementary);
            // 更新
            batchUpdate(intersection);
            return true;
        } catch (CloneNotSupportedException e ) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void batchInsert(TableStore store) throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        ContentProviderOperation.Builder operation;
        for (int r = 0; r < store.size(); r++) {
            operation = ContentProviderOperation.newInsert(contentUri);
            for (int c = 0; c < store.getWidth(); c++) {
                operation.withValue(store.field(c), store.retrieve(r, c));
            }
            operations.add(operation
                    .withYieldAllowed(true)
                    .build());
        }

        context.getContentResolver().applyBatch(authority, operations);
    }

    public void batchDelete(TableStore store) {

    }
    public void batchUpdate(TableStore store) {

    }

}
