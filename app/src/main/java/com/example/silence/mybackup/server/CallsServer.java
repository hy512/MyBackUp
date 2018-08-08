package com.example.silence.mybackup.server;

import android.content.AsyncQueryHandler;
import android.database.Cursor;
import android.provider.CallLog;
import android.support.v4.widget.ResourceCursorAdapter;

import com.example.silence.mybackup.util.TableStore;

import java.io.IOException;
import java.security.Permission;

public class CallsServer extends AbsBackupServer {

    @Override
    public TableStore load() {
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
//        Permission
//        Cursor cursor = resolver.query(CallLog.Calls.CONTENT_URI, fields, null, null, null);
        return null;
    }

    @Override
    public TableStore tidy() {
        return null;
    }

    @Override
    public void store(String path, TableStore store) throws IOException {

    }

    @Override
    public TableStore retrieve() {
        return null;
    }

    @Override
    public void sync() {

    }
}
