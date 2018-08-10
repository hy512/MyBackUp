package com.example.silence.mybackup.test;

import com.example.silence.mybackup.util.TableStore;



import java.util.Arrays;
import java.util.Objects;

public class Run {

//    @Test
    public void run() {
        TableStore store = new TableStore(new String[]{
                "年龄",
                "性别",
        }, new Class[]{
                Integer.class,
                String.class
        });
        store.insertRow(new Object[]{17, "再见"});
        store.insertRow(new Object[]{17, "再见"});

        System.out.println(store.toString());
        store.distinct(new int[]{0});
        System.out.println(store.toString());
        store.distinct(new int[0]);
        System.out.println(store.toString());


    }


//    @Test
    public void array() {
        int[] exclude = new int[]{-5, 1, 2, 4, 5, 6};
        System.out.println(Arrays.binarySearch(exclude, 3));
    }


}
