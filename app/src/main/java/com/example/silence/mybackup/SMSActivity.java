package com.example.silence.mybackup;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.silence.mybackup.app.ActivityBackupServer;
import com.example.silence.mybackup.entiry.MyContact;
import com.example.silence.mybackup.server.BackupServer;
import com.example.silence.mybackup.server.ShortMessageServer;
import com.example.silence.mybackup.util.SimpleStore;
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

public class SMSActivity extends ActivityBackupServer {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);
        server = new ShortMessageServer(this);

        // 判断权限，读取短信
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
            != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, ShortMessageServer.READ_SMS_REQUEST_CODE);
        else setContent(server.load());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i= permissions.length-1; i>=0; i--) {
            if (Manifest.permission.READ_SMS.equals(permissions[i]) &&
                    grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                setContent(server.load());
            }
        }
    }

    public void displayOptions() {
        List<ViewUtil.DialogOption> options = new ArrayList<>();


        options.add(new ViewUtil.DialogOption(
           "备份",
                (View v) -> {
                    try {
                        String path = getBackupPath();
                        server.store(path + "/ms.json", table);
                        Toast.makeText(SMSActivity.this, R.string.operate_result_success, Toast.LENGTH_SHORT).show();
                    } catch (IllegalStateException e) {
                        Toast.makeText(SMSActivity.this, R.string.directory_exception, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(SMSActivity.this, R.string.operate_result_failure, Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
        ));
        options.add(new ViewUtil.DialogOption(
                "还原",
                ( View v) ->{
                    try {
                        String path = getBackupPath();
                        TableStore store = server.retrieve(path + "/ms.json");
//                        Log.d("--->", store.toString());
//                        Log.d("--->", "size " + store.size());
                        appendContent(store);
                        Toast.makeText(SMSActivity.this, "操作成功, "+ store.size() + "条数据添加.", Toast.LENGTH_SHORT).show();
                    } catch (IllegalStateException e) {
                        Toast.makeText(SMSActivity.this, R.string.directory_exception, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(SMSActivity.this, R.string.operate_result_failure, Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }));

        options.add(new ViewUtil.DialogOption(
                "同步",
                (View v) ->{
                    server.sync(table);
                    Toast.makeText(SMSActivity.this, R.string.operate_result_success, Toast.LENGTH_SHORT).show();
                }));
        ViewUtil.dialogOptions(this, options).show();
    }



    @Override
    protected void mappingDataView() {
        ((TextView) findViewById(R.id.sample_text)).setText(table.toString());
    }
}
