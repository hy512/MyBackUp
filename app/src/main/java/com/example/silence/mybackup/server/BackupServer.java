package com.example.silence.mybackup.server;

import com.example.silence.mybackup.util.SimpleStore;
import com.example.silence.mybackup.util.TableStore;

import java.io.IOException;

public interface BackupServer {

    // 加载
     TableStore load();
    // 整理
     TableStore tidy();
    // 备份
     void store(String path, TableStore store) throws IOException;
    // 恢复
     TableStore retrieve();
    // 同步
     void sync();
}
