package com.example.silence.mybackup.server;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.example.silence.mybackup.ContactsActivity;
import com.example.silence.mybackup.entiry.MyContact;

import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerFactory;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.type.JavaType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

public class ContactServer {
    ContentResolver contentResolver;
    ObjectMapper mapper;

    public ContactServer() {
        mapper = new ObjectMapper();
    }

    public ContactServer(ContentResolver resolver) {
        this();
        this.contentResolver = resolver;
    }

    public List<MyContact> getContacts() {
        // 获取所有联系人
        List<Integer> contactIds = getContactIds();
        Log.d("--->", "读取 contacts " + contactIds.size());
        // 获取所有联系人信息
        Map<Integer, String> contactsName = getContacts(contactIds);
        Log.d("--->", "读取 rawcontacts " + contactsName.size());
        // 获取所有联系人具体信息
        List<MyContact> contacts = getContacts(contactsName);
        Log.d("--->", "读取 data " + contacts.size());
        return contacts;
    }

    public List<MyContact> getContacts(ContentResolver resolver) {
        this.contentResolver = resolver;
        return getContacts();
    }

    public String getContactsAsJSON() {
        try {
            return mapper.writeValueAsString(this.getContacts());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 查询 contacts 表
    public List<Integer> getContactIds() {
        List<Integer> contactIds = new ArrayList<Integer>();
        // 查询 contacts 表
        Cursor contacts = this.contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                new String[]{ContactsContract.Contacts._ID},
                null,
                null,
                null
        );
        while (contacts.moveToNext()) {
            // 数据从 0 开始
            int id = contacts.getInt(0);
            contactIds.add(id);
        }
        contacts.close();
        return contactIds;
    }

    // 查询 raw_contacts 表
    public Map<Integer, String> getContacts(List<Integer> contactIds) {
        Map<Integer, String> contacts = new HashMap<Integer, String>();
        Iterator<Integer> iterator = contactIds.iterator();
        while (iterator.hasNext()) {
            Cursor cursor = this.contentResolver.query(
                    ContactsContract.RawContacts.CONTENT_URI,
                    new String[]{
                            ContactsContract.RawContacts._ID,
                            ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY
                    },
                    String.format("%s=?", ContactsContract.RawContacts.CONTACT_ID),
                    new String[]{iterator.next() + ""},
                    null);

            while (cursor.moveToNext()) {
                contacts.put(cursor.getInt(0), cursor.getString(1));
            }
            cursor.close();
        }
        return contacts;
    }

    // 查询 data 表
    public List<MyContact> getContacts(Map<Integer, String> contactName) {
        List<MyContact> contacts = new ArrayList<MyContact>();
        for (Map.Entry<Integer, String> id : contactName.entrySet()) {
            // 联系人对象
            MyContact contact = new MyContact();
            contact.setRaw_contact_id(id.getKey());
            contact.setName(id.getValue());
            // 开始遍历属性
            Cursor cursor = this.contentResolver.query(
                    ContactsContract.Data.CONTENT_URI,
                    new String[]{
                            ContactsContract.Data.MIMETYPE,
                        ContactsContract.Data.DATA1
                    },
                    String.format("%s=?", ContactsContract.Data.RAW_CONTACT_ID),
                    new String[]{id.getKey() + ""},
                    null);
            if (cursor != null)
                while (cursor.moveToNext()) {
                    // 将属性加入联系人对象
                    contact.addProperty(cursor.getString(0), cursor.getString(1));
                }
            // 加入 list
            contacts.add(contact);
            cursor.close();
        }
        return contacts;
    }


//    public void sync(List<MyContact> contacts) {
//        Iterator<MyContact> iterator = contacts.iterator();
//
//        while (iterator.hasNext()) {
//            MyContact myContact = iterator.next();
//            ContentValues contact = new ContentValues();
//            // 在联系人 row_contacts 表插入记录，获取 _id 属性
//            long contact_id = ContentUris.parseId(this.contentResolver.insert(
//                    ContactsContract.RawContacts.CONTENT_URI, contact
//            ));
//            // 插入联系人的各个属性到 dta
//            for (int i=myContact.getProperties().size()-1; i>=0; i--) {
//                contact.put(ContactsContract.Contacts.Data.RAW_CONTACT_ID, contact_id);
//                contact.put(ContactsContract.Contacts.Data.MIMETYPE,  myContact.getPropKey(i));
//                contact.put(ContactsContract.Contacts.Data.DATA1, myContact.getPropValue(i));
//                this.contentResolver.insert(ContactsContract.Data.CONTENT_URI, contact);
//                contact.clear();
//            }
//        }
//    }

    public void sync(List<MyContact> contacts) {
        // 最新记录关于本地记录的相对补集，不被需要了。
        ArrayList<MyContact> complementary = (ArrayList<MyContact>) this.getContacts();
        // 本地记录与最新记录的交集，可能需要更新的内容
        ArrayList<MyContact> intersection = (ArrayList<MyContact>) ((ArrayList<MyContact>) contacts).clone();
        intersection.retainAll(contacts);
        complementary.removeAll(intersection);
        // 本地记录关于最新记录的相对补集，需要插入的内容
        contacts.removeAll(intersection);

        // 过滤需要更新的元素
        ListIterator<MyContact> iterator = intersection.listIterator();
        while (iterator.hasNext()) {
            MyContact contact = iterator.next();
            if (!contact.isChanged()) iterator.remove();
        }


        // 执行更改
        try {
            insertContacts(contacts);
            // updateContacts(intersection);
            deleteContacts(complementary);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    public void insertContacts(List<MyContact> contacts) throws RemoteException, OperationApplicationException {
        Iterator<MyContact> iterator = contacts.iterator();
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        // 插入操作
        while (iterator.hasNext()) {
            MyContact myContact = iterator.next();
            // 插入的 raw
            operations.add(ContentProviderOperation
                    .newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());
            // 当前操作 raw contact 的位置
            int rawIndex = operations.size() - 1;
            // 添加 data 数据
            for (int i = myContact.getProperties().size() - 1; i >= 0; i--) {
                operations.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, myContact.getPropKey(i))
                        .withValue(ContactsContract.Data.DATA1, myContact.getPropValue(i))
                        .build());
            }
        }
        this.contentResolver.applyBatch(ContactsContract.AUTHORITY, operations);
    }

    public void deleteContacts(List<MyContact> contacts) throws RemoteException, OperationApplicationException {
        Iterator<MyContact> iterator = contacts.iterator();
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        while (iterator.hasNext()) {
            MyContact contact = iterator.next();
            // 删除 raw
            operations.add(ContentProviderOperation
                    .newDelete(ContactsContract.RawContacts.CONTENT_URI)
                    .withSelection(String.format("%s=?", ContactsContract.RawContacts._ID), new String[]{contact.getRaw_contact_id().toString()})
                    .build());
            // 删除 data
            operations.add(ContentProviderOperation
                    .newDelete(ContactsContract.Data.CONTENT_URI)
                    .withSelection(String.format("%s=?", ContactsContract.Data.RAW_CONTACT_ID), new String[]{contact.getRaw_contact_id().toString()})
                    .build());
        }
        this.contentResolver.applyBatch(ContactsContract.AUTHORITY, operations);
    }

    public void updateContacts(List<MyContact> contacts) {
        Iterator<MyContact> iterator = contacts.iterator();
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        while (iterator.hasNext()) {
            MyContact contact = iterator.next();

            for (int i = contact.getProperties().size() - 1; i >= 0; i--)
                operations.add(ContentProviderOperation
                        .newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(String.format("%s=? and %s=?", ContactsContract.Data.RAW_CONTACT_ID, ContactsContract.Data._ID), new String[]{})
                        .build());
        }
    }

    public void store(String path) throws IOException {
        File contacts = new File(path);
        FileOutputStream outputStream = null;
        PrintWriter print = null;

        try {
            if (!contacts.exists()) {
                File parent = contacts.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                contacts.createNewFile();
            }
            outputStream = new FileOutputStream(contacts);
            print = new PrintWriter(outputStream);
            print.print(this.getContactsAsJSON());
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (print != null) print.close();
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void store(String path, List<MyContact> myContacts) throws IOException {
        File contacts = new File(path);
        FileOutputStream outputStream = null;
        PrintWriter print = null;

        try {
            if (!contacts.exists()) {
                File parent = contacts.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                contacts.createNewFile();
            }
            outputStream = new FileOutputStream(contacts);
            print = new PrintWriter(outputStream);
            print.print(mapper.writeValueAsString(myContacts));
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (print != null) print.close();
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 通过名称排序
    public void sortContactsByName(List<MyContact> contacts) {
        Collections.sort(contacts, (MyContact o1, MyContact o2) -> {
            String n1 = o1.getName();
            String n2 = o2.getName();
            // 比较完全相等
            if (n1.equals(n2)) {
                return 0;
            }
            Collator collate = Collator.getInstance(Locale.CHINA);
            return collate.compare(n1, n2);
            // 逐字比较
            /*
            for (int i = 0, limit = n1.length() < n2.length() ? n1.length() : n2.length();
                 i < limit;
                 i++) {
                if (n1.charAt(i) == n2.charAt(i))
                    continue;
                else if (n1.charAt(i) < n2.charAt(i))
                    return -1;
                else
                    return 1;
            }
            // 比较长度
            return n1.length() > n2.length() ? 1 : -1;
            */
        });
    }

    public ArrayList<MyContact> retrieve(String path) throws IOException {
        File contacts = new File(path);
        FileReader reader = null;
        BufferedReader bufferedReader = null;

        try {
            if (!contacts.exists()) {
                throw new FileNotFoundException(String.format("未发现文件在 %s .", path));
            }

            // 读取 json 文件数据
            reader = new FileReader(contacts);
            bufferedReader = new BufferedReader(reader);
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) builder.append(line);
            // 解析 json
            CollectionType type = mapper.getTypeFactory().constructCollectionType(ArrayList.class, MyContact.class);
            ArrayList<MyContact> myContacts = mapper.readValue(builder.toString(), type);
            return myContacts;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (bufferedReader != null) bufferedReader.close();
            if (reader != null) reader.close();
        }
    }

    public List<MyContact> tidy(List<MyContact> contacts) {
        this.sortContactsByName(contacts);
        ListIterator<MyContact> iterator = contacts.listIterator();

        MyContact prev;
        MyContact next;
        if (iterator.hasNext()) {
            prev = iterator.next();
            while (iterator.hasNext()) {
                next = iterator.next();
                // 当两条数据名称相等
                if (prev.getName().trim().equals(next.getName().trim())) {
                    prev.merge(next);
                    prev.getProperties().distinct();
                    Log.d("--->", prev.getProperties().toString());
                    iterator.remove();
                } else {
                    prev = next;
                }
            }
        }
        return contacts;
    }
}
