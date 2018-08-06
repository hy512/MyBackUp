package com.example.silence.mybackup;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

public class ContactActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        // 获取传递信息
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        List<String> phones = intent.getStringArrayListExtra("phones");

        // 判断为空
        if (name == null || phones == null) {
            // 结束程序
            finish();
        }
        // 设置信息到视图
        ((TextView)findViewById(R.id.contact_name)).setText(name);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                ContactActivity.this,
                android.R.layout.simple_list_item_1,
                phones);
        ((ListView)findViewById(R.id.phone_list)).setAdapter(adapter);
    }
}
