package com.example.silence.mybackup;

import android.app.ActivityGroup;
import android.app.LocalActivityManager;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;

import com.example.silence.mybackup.server.AbsBackupServer;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 确定存在程序储存目录，否则设置默认目录
        // 认定 path 中是不存在结尾 / 符号
        SharedPreferences preferences = getSharedPreferences("mybackup", Context.MODE_PRIVATE);
        if (preferences.getString("path", null) == null) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                // 设置默认目录
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("path", Environment.getExternalStorageDirectory().getAbsolutePath());
                editor.commit();
            } else {
                // 没有储存
                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).create();
                dialog.setTitle("启动失败");
                dialog.setMessage("没有可用储存空间，请确认手机储存或 SD 卡状态.");
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, "退出程序", (DialogInterface target, int which) -> {
                    MainActivity.this.finish();
                });
                dialog.show();
            }
        }

        // TabHost 使用的是懒加载的模式，只有打开 Tab 对应 activity 才会初始化
        TabHost host = findViewById(R.id.tabhost);

        LocalActivityManager localActivity = new LocalActivityManager(MainActivity.this, false);
        localActivity.dispatchCreate(savedInstanceState);
        host.setup(localActivity);

        host.addTab(
                host.newTabSpec("messages")
                        .setIndicator("短信")
                        .setContent(new Intent(
                                MainActivity.this,
                                SMSActivity.class
                        )));

        host.addTab(
                host.newTabSpec("contacts")
                        .setIndicator("联系人")
                        .setContent(new Intent(
                                MainActivity.this,
                                ContactsActivity.class)));

        host.addTab(host.newTabSpec("calls")
                .setIndicator("通话记录")
                .setContent(new Intent(
                        MainActivity.this,
                        CallsActivity.class
                )));

        host.addTab(
                host.newTabSpec("preferences")
                        .setIndicator("设置")
                        .setContent(new Intent(
                                MainActivity.this,
                                PreferencesActivity.class
                        )));
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
