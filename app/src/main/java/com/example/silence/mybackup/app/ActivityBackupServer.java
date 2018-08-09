package com.example.silence.mybackup.app;

import android.content.Context;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.example.silence.mybackup.CallsActivity;
import com.example.silence.mybackup.R;
import com.example.silence.mybackup.server.BackupServer;
import com.example.silence.mybackup.util.TableStore;

public abstract class ActivityBackupServer extends AppCompatActivity {
    protected TableStore table;
    protected BackupServer server ;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            displayOptions();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public abstract void displayOptions();

    public String getBackupPath() throws IllegalStateException {
        String path = getSharedPreferences("mybackup", Context.MODE_PRIVATE).getString("path", null);
        if (path == null) throw new IllegalStateException();
        return path;
    }

    public void setContent(TableStore table) {
        this.table = table;
        mappingDataView();
    }
    public void appendContent(TableStore table) {
        this.table.addAll(table);
        mappingDataView();
    }
    protected abstract void mappingDataView() ;
}
