package com.example.silence.mybackup.util;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.silence.mybackup.ContactActivity;
import com.example.silence.mybackup.R;
import com.example.silence.mybackup.entiry.MyContact;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends ArrayAdapter {
    private int resourceId;
    private Context context;
    private ObjectMapper mapper = new ObjectMapper();

    public ContactAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    public ContactAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List objects) {
        super(context, resource, objects);
        this.context = context;
        this.resourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        MyContact contact = (MyContact) getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        // 点击时候的跳转和信息传递
        view.setOnClickListener((View v) -> {
            Intent intent = new Intent(this.context, ContactActivity.class);
            intent.putExtra("name", contact.getName());
            intent.putStringArrayListExtra("phones", (ArrayList<String>) contact.getPhones());
            context.startActivity(intent);
        });
        // 给组件设置内容
        ((TextView) view.findViewById(R.id.contact_name)).setText(contact.getName());
        ((TextView) view.findViewById(R.id.contact_phone)).setText(contact.getPhone());
        return view;
    }
}
