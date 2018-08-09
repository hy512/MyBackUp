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
import android.util.Log;

import com.example.silence.mybackup.util.TableStore;

import java.io.IOException;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

import static android.provider.CalendarContract.CalendarCache.URI;

public class CallsServer extends AbsBackupServer {
    public static int READ_CALL_LOG_REQUEST_CODE = 300;
    Activity context;

    public CallsServer(Activity activity) {
        this.context = activity;
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
                CallLog.Calls.GEOCODED_LOCATION
        };

        TableStore table = new TableStore(fields);
        Cursor cursor = resolver.query(Uri.parse("content://call_log/calls"), fields, null, null, null);
        while (cursor.moveToNext()) {
            table.insertRow(new Object[]{
                    cursor.getLong(cursor.getColumnIndex(table.field(0))),
                    cursor.getInt(cursor.getColumnIndex(table.field(1))),
                    cursor.getString(cursor.getColumnIndex(table.field(2))),
                    cursor.getLong(cursor.getColumnIndex(table.field(3))),
                    cursor.getInt(cursor.getColumnIndex(table.field(4))),
                    cursor.getInt(cursor.getColumnIndex(table.field(5))),
                    cursor.getInt(cursor.getColumnIndex(table.field(6))),
                    cursor.getString(cursor.getColumnIndex(table.field(7))),
                    cursor.getString(cursor.getColumnIndex(table.field(8))),
                    cursor.getString(cursor.getColumnIndex(table.field(9)))
            });
        }
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

    @Override
    public TableStore tidy(TableStore store) {
        return null;
    }


    @Override
    public void sync(TableStore store) {
        try {
            batchInsert(store);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    public void batchInsert(TableStore store) throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> options = new ArrayList<>();

        // 遍历 tab 值
        ContentProviderOperation.Builder option;
        for (int r = 0; r < store.size(); r++) {
            option = ContentProviderOperation.newInsert(Uri.parse("content://call_log/calls"));
            for (int c = 0; c < store.getWidth(); c++) {
                option.withValue(store.field(c), store.retrieve(r, c));
            }
            options.add(option.withYieldAllowed(true).build());
        }

        context.getContentResolver().applyBatch(CallLog.AUTHORITY, options);
    }


}
