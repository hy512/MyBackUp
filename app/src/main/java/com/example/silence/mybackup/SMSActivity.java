package com.example.silence.mybackup;

import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.TextView;

import com.example.silence.mymessage.entiry.PhoneSMS;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SMSActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);




        tv.setText(builder.toString());
    }

}
