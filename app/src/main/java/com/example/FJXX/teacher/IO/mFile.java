package com.example.FJXX.teacher.IO;

import android.os.Handler;
import android.widget.EditText;

import java.util.List;

public interface mFile {
    void SendFile(Handler handler, String ip, EditText url, int fileType, int delType, List<String> files);
}
