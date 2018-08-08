package com.example.silence.mybackup.server;

import android.content.ContentResolver;

import org.codehaus.jackson.map.ObjectMapper;

public abstract class AbsBackupServer implements BackupServer {
    protected static ContentResolver resolver;
    protected static ObjectMapper mapper = new ObjectMapper();
}
