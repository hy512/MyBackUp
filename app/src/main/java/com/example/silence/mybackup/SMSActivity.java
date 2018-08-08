package com.example.silence.mybackup;

import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.example.silence.mybackup.server.BackupServer;
import com.example.silence.mybackup.server.ShortMessageServer;
import com.example.silence.mybackup.util.TableStore;
import com.example.silence.mybackup.util.ViewUtil;
import com.example.silence.mymessage.entiry.PhoneSMS;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SMSActivity extends AppCompatActivity {

    BackupServer server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);

        server =  ShortMessageServer.instance(getContentResolver());

        TableStore ms = server.load();

        tv.setText(ms.toString());
//        tv.setText(server.load());
        try {
            server.store("/storage/emulated/0/ms.json", ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            this.displayOptions();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void displayOptions() {
        List<ViewUtil.DialogOption> options = new ArrayList<>();

        options.add(new ViewUtil.DialogOption(
           "备份",
                (View v) -> {

                }
        ));
    }
}
