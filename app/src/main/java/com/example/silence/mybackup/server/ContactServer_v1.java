package com.example.silence.mybackup.server;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;

import com.example.silence.mybackup.util.TableStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class ContactServer_v1 extends AbsBackupServer {
    public static final int READ_CONTACTS_REQUEST_CODE = 200;

    public ContactServer_v1(Activity activity) {
        context = activity;
        contentUri = ContactsContract.Data.CONTENT_URI;
        authority = ContactsContract.AUTHORITY;
    }

    @Override
    public TableStore retrieve(String path) throws IOException {
        TableStore store = super.retrieve(path);
        store.insertColumnOfNull(0, ContactsContract.Data._ID);
        return store;
    }

    @Override
    public void store(String path, TableStore store) throws IOException {
        store.removeColumn(ContactsContract.Data._ID);
        super.store(path, store);
    }

    @Override
    public TableStore load() {
        String[] fields = new String[]{
                ContactsContract.Data._ID,
                ContactsContract.Data.RAW_CONTACT_ID, // 一样的 raw contact id 表示同一个联系人
                ContactsContract.Data.MIMETYPE,
                ContactsContract.Data.DATA1,
                ContactsContract.Data.DATA2,
                ContactsContract.Data.DATA3,
                ContactsContract.Data.DATA4,
                ContactsContract.Data.DATA5,
                ContactsContract.Data.DATA6,
                ContactsContract.Data.DATA7,
                ContactsContract.Data.DATA8,
                ContactsContract.Data.DATA9,
                ContactsContract.Data.DATA10,
                ContactsContract.Data.DATA11,
                ContactsContract.Data.DATA12,
                ContactsContract.Data.DATA13,
                ContactsContract.Data.DATA14
//                ContactsContract.Data.DATA15 // Blob 数据类型
        };

        TableStore store = new TableStore(fields);
        Object[] row;

        Cursor cursor = context.getContentResolver().query(contentUri, fields, null, null, null);
        while (cursor.moveToNext()) {
            row = new Object[fields.length];
            row[0] = cursor.getLong(store.field(fields[0]));
            for (int i = 1; i < fields.length; i++) {

                row[i] = cursor.getString(store.field(fields[i]));
            }
            store.insertRow(row);
        }
        cursor.close();


        return store;
    }

    @Override
    public TableStore tidy(TableStore store) {
        Collections.sort(store, (TableStore.Row o1, TableStore.Row o2) -> {
            String rawIdStr1 = o1.retrieve(store.field(ContactsContract.Data.RAW_CONTACT_ID));
            String rawIdStr2 = o2.retrieve(store.field(ContactsContract.Data.RAW_CONTACT_ID));

            // null 判断
            if (rawIdStr1 == rawIdStr2) return 0;
            if (rawIdStr1 == null) return -1;
            if (rawIdStr2 == null) return 1;

            long rawId1 = Long.parseLong(rawIdStr1);
            long rawId2 = Long.parseLong(rawIdStr2);
            // 大小判断
            if (rawId1 == rawId2) return 0;
            return rawId1 > rawId2 ? 1 : -1;
        });
        return store;
    }

    @Override
    public boolean sync(TableStore store) {

        try {
            // 避免改变列表数据
            store = store.clone();

            TableStore local = load();
            // 交集
            TableStore intersection = store.clone();
            intersection.retainAll(local);
            // 新内容关于本地内容的补集, 不被需要
            TableStore complementary = local;
            complementary.removeAll(intersection);
            local = null;
            // 本地内容关于新内容的相对补集, 需要添加
            store.removeAll(intersection);

            // 插入
            batchInsert(store);
            // 删除
            batchDelete(complementary);
            // 更新
            batchUpdate(intersection);
            return true;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void batchInsert(TableStore store) throws RemoteException, OperationApplicationException {
        // 排序
        store = tidy(store);

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        String rawId = null;
        int rawInsertPosition = 0;
        // 遍历 tab 值
        ContentProviderOperation.Builder operation;
        for (int r = 0; r < store.size(); r++) {
            // 先插入 rawcontact 表
            // 当  rawId == null 第一次插入，或当 rawId 不等于 store 中的值， 移动到了下一个 rawId，说明需要新建一个联系人信息了。
            if (rawId == null ||
                    !rawId.equals(store.retrieve(r, store.field(ContactsContract.Data.RAW_CONTACT_ID)))) {
                operations.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME,
                                null).withValue(ContactsContract.RawContacts.ACCOUNT_TYPE,
                                null).withYieldAllowed(true).build());
                // 更新插入位置与当前 rawId
                rawInsertPosition = operations.size() - 1;
                rawId = store.retrieve(r, store.field(ContactsContract.Data.RAW_CONTACT_ID));
            }
            // 先插入 rawId
            operation = ContentProviderOperation
                    .newInsert(contentUri)
                    .withValueBackReference(
                            ContactsContract.Data.RAW_CONTACT_ID,
                            rawInsertPosition);
            for (int c = 0; c < store.getWidth(); c++) {
                if (store.retrieve(r, c) != null &&
                        // 取消插入原有的 id 和 rawId
                        c != store.field(ContactsContract.Data._ID) &&
                        c != store.field(ContactsContract.Data.RAW_CONTACT_ID))
                    operation.withValue(store.field(c), store.retrieve(r, c));
            }
            operations.add(operation.build());
        }

        context.getContentResolver().applyBatch(authority, operations);
    }

    public void batchDelete(TableStore store) throws RemoteException, OperationApplicationException {
        store = tidy(store);

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        String rawId = null;

        ContentProviderOperation.Builder operation;
        for (int r = 0; r < store.size(); r++) {

            operation = ContentProviderOperation.newDelete(contentUri);
            if (store.retrieve(r, ContactsContract.Data._ID) != null) {
                operations.add(operation
                        .withSelection(ContactsContract.Data._ID.concat("=?"), new String[]{store.retrieve(r, ContactsContract.Data._ID).toString()})
                        .withYieldAllowed(true)
                        .build());
                // 准备删除 raw 表中内容
                if (store.retrieve(r, ContactsContract.Data._ID).equals(rawId)) {
                    if (rawId != null)
                        operations.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
                        .withSelection(ContactsContract.RawContacts._ID+"=?",
                                new String[]{rawId}).withYieldAllowed(true).build());
                    rawId = store.retrieve(r, ContactsContract.Data._ID);
                }
            }
        }

        context.getContentResolver().applyBatch(authority, operations);
    }

    public void batchUpdate(TableStore store) {
//        Array<ContentProviderOperation>
    }
}
