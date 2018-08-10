package com.example.silence.mybackup.server;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
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
                Telephony.Sms.THREAD_ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.PERSON,
                Telephony.Sms.DATE,
                Telephony.Sms.DATE_SENT,
                Telephony.Sms.PROTOCOL,
                Telephony.Sms.STATUS,
                Telephony.Sms.REPLY_PATH_PRESENT,
                Telephony.Sms.BODY,
                Telephony.Sms.SERVICE_CENTER,
                Telephony.Sms.LOCKED,
                Telephony.Sms.ERROR_CODE,
                Telephony.Sms.SEEN,
//
                Telephony.Sms.TYPE,
                Telephony.Sms.READ
        };
        Cursor cursor = resolver.query(contentUri, fields, null, null, null);
        TableStore ms = new TableStore(fields);


        while (cursor.moveToNext()) {
            Object[] row = new Object[fields.length];
            row[0] = cursor.getLong(cursor.getColumnIndex(ms.field(0)));
            for (int i=1; i<fields.length; i++)
                row[i] = cursor.getString(cursor.getColumnIndex(ms.field(i)));
            ms.insertRow(row);
        }
        cursor.close();
        return ms;
    }

    @Override
    public TableStore retrieve(String path) throws IOException {
        TableStore store = super.retrieve(path);
        store.insertColumnOfNull(0, Telephony.Sms._ID);
        return store;
    }

    @Override
    public void store(String path, TableStore store) throws IOException {
        store.removeColumn(Telephony.Sms._ID);
        super.store(path, store);
    }

    @Override
    public TableStore tidy(TableStore store) {
        return null;
    }


    @Override
    public boolean sync(TableStore store) {
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
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
        return false;
    }

//    public void batchInsert(TableStore store) throws RemoteException, OperationApplicationException {
//        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
//
//        ContentProviderOperation.Builder operation;
//        for (int r = 0; r < store.size(); r++) {
//            operation = ContentProviderOperation.newInsert(Uri.parse("content://sms/inbox"));
//            for (int c = 0; c < store.getWidth(); c++) {
//                if (store.retrieve(r, c) !=  null)
//                    operation.withValue(store.field(c), store.retrieve(r, c));
//            }
//            operations.add(operation
//                    .withYieldAllowed(true)
//                    .build());
//        }
//
//        context.getContentResolver().applyBatch(Uri.parse("content://sms/inbox").getAuthority(), operations);
//    }

    public void batchInsert(TableStore store) throws RemoteException, OperationApplicationException {
        ContentValues values;
        ContentResolver resolver = context.getContentResolver();
        for (int r = 0; r < store.size(); r++) {
            values = new ContentValues();
            for (int c = 0; c < store.getWidth(); c++) {
                if (store.retrieve(r, c) != null)
                    values.put(store.field(c), store.retrieve(r, c).toString());
            }
            resolver.insert(Uri.parse("content://sms/inbox"), values);
        }
    }

    public void batchDelete(TableStore store) {

    }

    public void batchUpdate(TableStore store) {

    }

}
