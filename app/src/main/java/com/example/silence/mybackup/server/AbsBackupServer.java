package com.example.silence.mybackup.server;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;

import com.example.silence.mybackup.util.TableStore;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;

public abstract class AbsBackupServer implements BackupServer {
    public static int READ_EXTERNAL_STORAGE_REQUEST_CODE = 1000;
    public static int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1100;
    protected static ObjectMapper mapper = new ObjectMapper();
    Activity context = null;
    Uri contentUri = null;
    String authority = null;

    @Override
    public TableStore retrieve(String path) throws  IOException {
        File json = new File(path);
        if (!json.exists()) throw new FileNotFoundException(path);
        return mapper.readValue(json, TableStore.class);
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
