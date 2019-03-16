package com.example.FJXX.teacher.IO;

import android.os.Handler;
import android.util.Log;
import android.widget.EditText;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.List;

public class SendMFile implements mFile {

    @Override
    public void SendFile(final Handler handler, final String ip, final EditText et_url, final int selectType, final int delType, final List<String> FileList) {
        new Thread() {
            @Override
            public void run() {
                Socket socket;
                long totalSize = 0;
                byte buf[] = new byte[8192];
                int len;
                try {
                    Log.d("准备连接服务器", ip);
                    socket = new Socket(ip, 8899);
                    System.out.println("连接服务器：" + socket.getInetAddress() + " ：" + new Date());
                    if (socket.isOutputShutdown()) {
                        return;
                    }
                    DataOutputStream dout = new DataOutputStream(
                            socket.getOutputStream());
                    /**
                     * @1 图片
                     * @2 视频
                     * @3 网页
                     */
                    dout.writeInt(selectType);
                    dout.flush();
                    //发送网页
                    if (selectType == 3) {
                        String Web = String.valueOf(et_url.getText());
                        if (Web.equals("")) {
                            handler.sendEmptyMessage(1);
                        } else {
                            dout.writeUTF(Web);
                            Log.d("正在发送......", String.valueOf(new Date()));
                        }
                    } else if (selectType == 4) {
                        dout.writeInt(delType);
                        dout.flush();
                        Log.d("删除文件", String.valueOf(delType));
                    } else {   //发送文件
                        java.io.File[] files = new java.io.File[FileList.size()];
                        for (int i = 0; i < FileList.size(); i++) {
                            files[i] = new java.io.File(FileList.get(i));
                        }
                        Log.d("正在发送......", String.valueOf(new Date()));
                        dout.writeInt(files.length);
                        for (int i = 0; i < files.length; i++) {
                            dout.writeUTF(files[i].getName());
                            dout.flush();
                            dout.writeLong(files[i].length());
                            dout.flush();
                            totalSize += files[i].length();
                        }
                        dout.writeLong(totalSize);
                        for (int i = 0; i < files.length; i++) {
                            BufferedInputStream din = new BufferedInputStream(
                                    new FileInputStream(files[i]));
                            while ((len = din.read(buf)) != -1) {
                                //write(byte[]b,int off,int len)要求off+len<b.length且 off*len！=0否则会抛出（IndexOutOfBoundsException） 将b中len个字节按顺序写入输出流从b[off]开始，到b[off+len]Q:off为负是什么情况
                                dout.write(buf, 0, len);
                            }
                        }
                        Log.d("文件发送完成", String.valueOf(new Date()));
//                        handler.sendEmptyMessage(2);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}