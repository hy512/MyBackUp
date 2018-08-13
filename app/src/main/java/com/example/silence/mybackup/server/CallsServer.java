package com.example.silence.mybackup.server;

import android.Manifest;
import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ResourceCursorAdapter;
import android.telecom.Call;
import android.util.Log;

import com.example.silence.mybackup.util.TableStore;

import java.io.IOException;
import java.lang.reflect.Array;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.provider.CalendarContract.CalendarCache.URI;

public class CallsServer extends AbsBackupServer {
    public static int READ_CALL_LOG_REQUEST_CODE = 300;

    public CallsServer(Activity activity) {
        this.context = activity;
        authority = CallLog.AUTHORITY;
        contentUri = Uri.parse("content://call_log/calls");
    }

    @Override
    public TableStore load() {
        ContentResolver resolver = context.getContentResolver();
        String[] fields = new String[]{
                CallLog.Calls._ID,
                CallLog.Calls.TYPE,
                CallLog.Calls.NUMBER,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.NEW,
                CallLog.Calls.IS_READ,
                CallLog.Calls.VOICEMAIL_URI,
                CallLog.Calls.COUNTRY_ISO,
                CallLog.Calls.GEOCODED_LOCATION,
                CallLog.Calls.CACHED_FORMATTED_NUMBER,
                CallLog.Calls.CACHED_LOOKUP_URI,
                CallLog.Calls.CACHED_MATCHED_NUMBER,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.CACHED_NORMALIZED_NUMBER,
                CallLog.Calls.CACHED_NUMBER_LABEL,
                CallLog.Calls.CACHED_NUMBER_TYPE,
//                CallLog.Calls.CONTENT_ITEM_TYPE,
//                CallLog.Calls.CONTENT_TYPE,
                CallLog.Calls.NUMBER_PRESENTATION,
                CallLog.Calls.PHONE_ACCOUNT_COMPONENT_NAME
//                CallLog.Calls.VIA_NUMBER
        };

        TableStore table = new TableStore(fields);
        Cursor cursor = resolver.query(contentUri, fields, null, null, null);

        Object[] row ;
        while (cursor.moveToNext()) {
            row = new Object[fields.length];
            row[0] = cursor.getLong(cursor.getColumnIndex(table.field(0)));
            for (int i=1; i<fields.length; i++)
                row[i] = cursor.getString(cursor.getColumnIndex(table.field(i)));
            table.insertRow(row);
        }
        cursor.close();
        return table;
    }

    @Override
    public TableStore retrieve(String path) throws IOException {
        TableStore store = super.retrieve(path);
        store.insertColumnOfNull(0, CallLog.Calls._ID);
        return store;
    }

    @Override
    public void store(String path, TableStore store) throws IOException {
        store.removeColumn(CallLog.Calls._ID);
        super.store(path, store);
    }

    // 排序并去重
    @Override
    public TableStore tidy(TableStore store) {
        // 去重
        store.distinct(new int[]{
                store.field(CallLog.Calls._ID),
                store.field(CallLog.Calls.COUNTRY_ISO),
                store.field(CallLog.Calls.GEOCODED_LOCATION),
                store.field(CallLog.Calls.VOICEMAIL_URI)});
        // 排序
        Collections.sort(store, (TableStore.Row o1, TableStore.Row o2) -> {
            // 相等
            if (o1.equals(o2)) return 0;
            // 比较 id
            {
                Long id1 = o1.retrieve(store.field(CallLog.Calls._ID));
                Long id2 = o2.retrieve(store.field(CallLog.Calls._ID));
                // id 值大的就大
                if (id1 != null && id2 != null)
                    if (id1.longValue() != id2.longValue())
                        return id1.longValue() > id2.longValue() ? 1 : -1;
            }
            // 比较 date
            {
                Long date1 = o1.retrieve(store.field(CallLog.Calls.DATE));
                Long date2 = o2.retrieve(store.field(CallLog.Calls.DATE));
                // date 值大就小
                if (date1 != null && date2 != null) {
                    if (!date1.equals(date2)) {
                        return date1.longValue() > date2.longValue() ? -1 : 1;
                    }
                }
                // date 为 null 者小
                else {
                    if (date1 == null) return -1;
                    if (date2 == null) return 1;
                }
            }
            return 0;
        });
        return store;
    }


    @Override
    public boolean sync(TableStore store) {
        try {
            store = store.clone();

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

    public void batchInsert(TableStore store) throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> options = new ArrayList<>();

        // 遍历 tab 值
        ContentProviderOperation.Builder option;
        for (int r = 0; r < store.size(); r++) {
            option = ContentProviderOperation.newInsert(contentUri);
            for (int c = 0; c < store.getWidth(); c++) {
                if (store.retrieve(r, c) != null)
                    option.withValue(store.field(c), store.retrieve(r, c));
            }
            options.add(option.withYieldAllowed(true).build());
        }

        context.getContentResolver().applyBatch(authority, options);
    }

    public void batchDelete(TableStore store) throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        ContentProviderOperation.Builder operation;
        for (int r = 0; r < store.size(); r++) {
            operation = ContentProviderOperation.newDelete(contentUri);
            if (store.retrieve(r, CallLog.Calls._ID) != null)
                operations.add(operation
                        .withSelection(CallLog.Calls._ID.concat("=?"), new String[]{store.retrieve(r, CallLog.Calls._ID).toString()})
                        .withYieldAllowed(true)
                        .build());
        }

        context.getContentResolver().applyBatch(authority, operations);
    }

    public void batchUpdate(TableStore store) {
//        Array<ContentProviderOperation>
    }
}
