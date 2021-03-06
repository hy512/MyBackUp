package com.example.silence.mybackup;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.silence.mybackup.app.ActivityBackupServer;
import com.example.silence.mybackup.server.CallsServer;
import com.example.silence.mybackup.server.ContactServer_v1;
import com.example.silence.mybackup.util.TableStore;
import com.example.silence.mybackup.util.ViewUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ContactsActivity_v1 extends ActivityBackupServer {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_v1);
        server = new ContactServer_v1(this);

        // 判断权限，读取通话记录
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    ContactServer_v1.READ_CONTACTS_REQUEST_CODE);
        else setContent(server.load());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = permissions.length - 1; i >= 0; i--) {
            if (Manifest.permission.READ_CONTACTS.equals(permissions[i]) &&
                    grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                setContent(server.load());
            }
        }
    }

    @Override
    public void displayOptions() {
        List<ViewUtil.DialogOption> options = new ArrayList<>();
        options.add(new ViewUtil.DialogOption(
                R.string.list_options_tidy,
                (View v) -> {
                    TableStore store = server.tidy(table);
                    setContent(store);
                    Toast.makeText(ContactsActivity_v1.this, String.format("操作成功, 数量 %d.", store.size()), Toast.LENGTH_SHORT).show();
                }));
        options.add(new ViewUtil.DialogOption(
                "清空",
                (View v) -> {
                    table.clear();
                    setContent(table);
                }));
        options.add(new ViewUtil.DialogOption(
                "备份",
                (View v) -> {
                    try {
                        String path = getBackupPath();
                        server.store(path + "/contacts.json", table);
                        Toast.makeText(ContactsActivity_v1.this, R.string.operate_result_success, Toast.LENGTH_SHORT).show();
                    } catch (IllegalStateException e) {
                        Toast.makeText(ContactsActivity_v1.this, R.string.directory_exception, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(ContactsActivity_v1.this, R.string.operate_result_failure, Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }));
        options.add(new ViewUtil.DialogOption(
                "还原",
                (View v) -> {
                    try {
                        String path = getBackupPath();
                        TableStore store = server.retrieve(path + "/contacts.json");
                        appendContent(store);
                        Toast.makeText(ContactsActivity_v1.this, "操作成功, " + store.size() + "条数据添加.", Toast.LENGTH_SHORT).show();
                    } catch (IllegalStateException e) {
                        Toast.makeText(ContactsActivity_v1.this, R.string.directory_exception, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(ContactsActivity_v1.this, R.string.operate_result_failure, Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }));
        options.add(new ViewUtil.DialogOption(
                "同步",
                (View v) -> {
                    server.sync(table);
                    Toast.makeText(ContactsActivity_v1.this, R.string.operate_result_success, Toast.LENGTH_SHORT).show();
                }));

        ViewUtil.dialogOptions(this, options).show();
    }

    @Override
    protected void mappingDataView() {
        ((TextView) findViewById(R.id.text)).setText(table.toString());
    }
}
