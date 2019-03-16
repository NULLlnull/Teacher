package com.example.FJXX.teacher;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.FJXX.teacher.IO.mFile;
import com.example.FJXX.teacher.IO.SendMFile;
import com.example.FJXX.teacher.Utils.GetPathFromUri;
import com.example.FJXX.teacher.Utils.SD_Permission;
import com.leon.lfilepickerlibrary.LFilePicker;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener, AdapterView.OnItemSelectedListener {
    private String SDPATH;
    private final int REQUESTCODE_FROM_ACTIVITY = 1000;
    private EditText et_ip, et_url;
    private RadioGroup rg;
    private RadioButton rb_web, rb_mv, rb_pic, rb_del;
    private Button btn_select, btn_submit, btn_media;
    private TextView tv_file;
    private int selectType = -1;
    private int delType = -1;
    private Spinner sp_del;
    private String[] dels = {"仅删除图片", "仅删除视频", "仅删除网页", "全部删除"};
    private String VIDEOPATH;

    private mFile file;
    private int nFile = 0;
    private String[] ips;

    private List<String> FileList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    private void initData() {
        SD_Permission.verifyStoragePermissions(this);
        sp_del.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, dels));
        file = new SendMFile();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && null != data) {
            if (requestCode == REQUESTCODE_FROM_ACTIVITY) {
                FileList = data.getStringArrayListExtra("paths");
                //显示当前有多少文件已被选择
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < FileList.size(); i++) {
                    String[] path = FileList.get(i).split("/");
                    builder.append(path[path.length - 1] + ",");
                }
                tv_file.setText("当前选择总文件数为" + FileList.size() + "：" + builder.toString());
//                Toast.FileList(getApplicationContext(), "selected " + list.size(), Toast.LENGTH_SHORT).show();
//                String path = data.getStringExtra("path");
//                Toast.makeText(getApplicationContext(), "The selected path is:" + path, Toast.LENGTH_SHORT).show();
            }
            if (requestCode == 2) {
                Uri selectedVideo = data.getData();
                String[] filePathColumn = {MediaStore.Video.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedVideo,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                VIDEOPATH = cursor.getString(columnIndex);
                FileList = new ArrayList<>();
                FileList.add(VIDEOPATH);
                tv_file.setText(VIDEOPATH);
            }
        }
    }

    private void initView() {
        et_url = findViewById(R.id.et_url);
        tv_file = findViewById(R.id.tv_file);
        et_ip = findViewById(R.id.et_ip);
        rg = findViewById(R.id.rg_type);
        rg.setOnCheckedChangeListener(this);
        rb_mv = findViewById(R.id.rb_mv);
        rb_web = findViewById(R.id.rb_web);
        rb_pic = findViewById(R.id.rb_pic);
        rb_del = findViewById(R.id.rb_del);
        btn_select = findViewById(R.id.btn_select);
        btn_select.setOnClickListener(this);
        btn_submit = findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(this);
        btn_media = findViewById(R.id.btn_media);
        btn_media.setOnClickListener(this);
        sp_del = findViewById(R.id.sp_del);
        sp_del.setOnItemSelectedListener(this);
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Toast.makeText(MainActivity.this, "请输入要发送的URL地址", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    if (nFile != FileList.size()) {
                        file.SendFile(handler, ips[nFile++], et_url, selectType, delType, FileList);
                    }
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_select:
                new LFilePicker()
                        .withActivity(MainActivity.this)
                        .withRequestCode(REQUESTCODE_FROM_ACTIVITY)
                        .withStartPath(String.valueOf(Environment.getExternalStorageDirectory()))
                        .withIsGreater(false)
                        .withFileSize(500 * 1024)
                        .withMutilyMode(true)
                        .start();
                break;
            case R.id.btn_submit:
                if (et_ip.getText().toString().isEmpty()) {
                    Toast.makeText(this, "请输入IP", Toast.LENGTH_SHORT).show();
                } else if (FileList == null && selectType != 3 && selectType != 4) {
                    Toast.makeText(this, "请先选择文件", Toast.LENGTH_SHORT).show();
                } else if (selectType == -1) {
                    Toast.makeText(this, "请先选择文件类型", Toast.LENGTH_SHORT).show();
                } else {
                    ips = et_ip.getText().toString().split(",");
//                    for (String ip : ips) {
//                        file.SendFile(handler, ip, et_url, selectType, delType, FileList);
//                    }
                    handler.sendEmptyMessage(2);
                }
                break;
            case R.id.btn_media:
                Intent intent = new Intent(Intent.ACTION_PICK);
                if (selectType == 1) {
                    //图片
                    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                } else if (selectType == 2) {
                    //视频
                    intent.setDataAndType(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "video/*");
                } else {
                    Toast.makeText(this, "请选择图片或视频", Toast.LENGTH_SHORT).show();
                    return;
                }
                startActivityForResult(intent, 2);
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        int count = rg.getChildCount();
        for (int i = 0; i < count; i++) {
            RadioButton rb = (RadioButton) rg.getChildAt(i);
            if (rb.isChecked()) {
                selectType = i + 1;
                if (selectType == 3) {
                    et_url.setVisibility(View.VISIBLE);
                } else {
                    et_url.setVisibility(View.GONE);
                }
                if (selectType == 4) {
                    sp_del.setVisibility(View.VISIBLE);
                } else {
                    sp_del.setVisibility(View.GONE);
                }
                break;
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        delType = position + 1;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
