package com.example.silence.mybackup;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.silence.mybackup.entiry.MyContact;
import com.example.silence.mybackup.util.ContactAdapter;
import com.example.silence.mybackup.util.ViewUtil;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ContactsActivity extends AppCompatActivity {
    com.example.silence.mybackup.server.ContactServer contactServer;
    List<MyContact> contacts;

    public void setContacts(List<MyContact> contacts) {
        contactServer.sortContactsByName(contacts);
        transformToList(contacts);
        this.contacts = contacts;
    }

    public void appendContacts(List<MyContact> contacts) {
        contacts.addAll(this.contacts);
        contactServer.sortContactsByName(contacts);
        transformToList(contacts);
        this.contacts = contacts;
    }

    // 将 list 插入到列表
    protected void transformToList(List<MyContact> contacts) {
        ListView list = findViewById(R.id.contact_list);
        ContactAdapter adapter = new ContactAdapter(ContactsActivity.this, R.layout.listview_contacts, contacts);
        list.setAdapter(adapter);
    }

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        // 读取联系人
        contactServer = new com.example.silence.mybackup.server.ContactServer(this.getContentResolver());
        List<com.example.silence.mybackup.entiry.MyContact> contacts = contactServer.getContacts();
        setContacts(contacts);

        Toast.makeText(this, String.format("读取联系人 %d.", contacts.size()), Toast.LENGTH_SHORT).show();
    }




    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 监听菜单键，弹出选单
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            this.displayOptions();
            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }


    // 监听菜单键的面板
    private void displayOptions() {
        List<ViewUtil.DialogOption> options = new ArrayList<>();
        options.add(new ViewUtil.DialogOption(
                R.string.list_options_tidy,
                (View v) -> {
                    setContacts(this.contactServer.tidy(this.contacts));
//                    Log.d("--->", this.contacts.toString());
                    Toast.makeText(ContactsActivity.this, String.format("合并成功.联系人数量 %d.", this.contacts.size()), Toast.LENGTH_SHORT).show();
                }));
        options.add(new ViewUtil.DialogOption(
                "清空",
                (View v) -> setContacts(new ArrayList<MyContact>())));
        options.add(new ViewUtil.DialogOption(
                R.string.list_options_backup,
                (View v) ->{
                    try {
                        String path = getSharedPreferences("mybackup", Context.MODE_PRIVATE).getString("path", null);
                        if (path == null) {
                            Toast.makeText(ContactsActivity.this, "程序储存目录异常，请重新设置.", Toast.LENGTH_SHORT).show();
                            throw new IOException("程序储存目录异常，请重新设置.");
                        }
                        this.contactServer.store(String.format("%s/contacts.json", path), this.contacts);
                        Toast.makeText(ContactsActivity.this, "备份完成", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(ContactsActivity.this, "操作失败", Toast.LENGTH_SHORT).show();
                    }}));
        options.add(new ViewUtil.DialogOption(
                "还原",
                ( View v) ->{
                    try {
                        String path = getSharedPreferences("mybackup", Context.MODE_PRIVATE).getString("path", null);
                        if (path == null) {
                            Toast.makeText(ContactsActivity.this, "程序储存目录异常，请重新设置.", Toast.LENGTH_SHORT).show();
                            throw new IOException("程序储存目录异常，请重新设置.");
                        }
                        List<MyContact> contacts = this.contactServer.retrieve(String.format("%s/contacts.json", path));
                        appendContacts(contacts);
                        Toast.makeText(ContactsActivity.this, String.format("载入完成, 添加 %d 个联系人.", contacts.size()), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(ContactsActivity.this, "操作失败", Toast.LENGTH_SHORT).show();
                    }
                }));
        options.add(new ViewUtil.DialogOption(
                "同步",
                (View v) ->{
                    this.contactServer.sync(this.contacts);
                    Toast.makeText(ContactsActivity.this, "同步成功", Toast.LENGTH_SHORT).show();
                }));

        ViewUtil.dialogOptions(ContactsActivity.this, options).show();
    }

}
