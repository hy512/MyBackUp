package com.example.silence.mybackup.util;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import com.example.silence.mybackup.ContactActivity;
import com.example.silence.mybackup.R;
import com.example.silence.mybackup.entiry.MyContact;

import org.codehaus.jackson.map.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter2 extends ArrayAdapter {
    private int resourceId;
    private  Context context;
    private ObjectMapper mapper = new ObjectMapper();

    public ContactAdapter2(@NonNull Context context, int resource) {
        super(context, resource);
    }
    public ContactAdapter2(@NonNull Context context, @LayoutRes int resource, @NonNull List objects) {
        super(context, resource, objects);
        this.context = context;
        this.resourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        ViewHolder holder;
        MyContact contact = (MyContact)getItem(position);
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            view.setOnClickListener((View v) -> {
//                try {
                    Intent intent = new Intent(this.context, ContactActivity.class);
                    intent.putExtra("name", contact.getName());
//                    intent.putExtra("phones", mapper.writeValueAsString(contact.getPhones()));
                    intent.putStringArrayListExtra("phones", (ArrayList<String>) contact.getPhones());
                    context.startActivity(intent);
//                } catch (IOException e) {
//                    Log.i("IOException: ", e.getMessage() + "\n" +e.getStackTrace().toString());
//                    Toast.makeText(ContactAdapter.this.context, e.getMessage(), Toast.LENGTH_SHORT).show();
//                }
            });
            holder = new ViewHolder();
            holder.contactName = (TextView)view.findViewById(R.id.contact_name);
            holder.contactPhone = (TextView)view.findViewById(R.id.contact_phone);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }
        holder.contactName.setText(contact.getName());
        holder.contactPhone.setText(contact.getPhone());
        return view;
    }
    class ViewHolder {
        TextView contactName;
        TextView contactPhone;
    }
}
