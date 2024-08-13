package com.octopus.example;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.octopus.example.databinding.ActivityMainBinding;
import com.zhuchao.android.fbase.EventCourier;
import com.zhuchao.android.fbase.FileUtils;
import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.fbase.MethodThreadMode;
import com.zhuchao.android.fbase.TCourierSubscribe;
import com.zhuchao.android.session.base.BaseActivity;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private final String TAG ="MainActivity";
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ///EdgeToEdge.enable(this);
        ///setContentView(R.layout.activity_main);
        // 初始化绑定类实例
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        // 设置内容视图为绑定类根视图
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.button1.setOnClickListener(this);
        binding.button2.setOnClickListener(this);
        binding.button3.setOnClickListener(this);
        binding.button9.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.button1) {
            String apkUrl = "http://www.1234998.top/downloads/S2p.apk"; //需要从网络下载的APK URL地址
            OctopusUtils.startDownloadApkFrom(apkUrl, FileUtils.getDownloadDir(null));
        } else if (id == R.id.button2)///用户点击事件安装指定APK
        {
            String apkFileName = "/storage/emulated/0/Download/S2p.apk";//要静默安装的APK 完成路径名称
            OctopusUtils.startSilentInstallApk(this, apkFileName);
        } else if (id == R.id.button3) {
            String appPackageName = "";//要卸载的APP包名
            OctopusUtils.startSilentUninstallApk(this, appPackageName);
        }
        else if (id == R.id.button9) {
           //FileUtils.getDownloadDir("aaa");
            if(FileUtils.existFile("/storage/emulated/0/Download//S2p.apk"))
                Toast.makeText(this, "file exists", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "file not exists", Toast.LENGTH_SHORT).show();
        }
    }


    @TCourierSubscribe(threadMode = MethodThreadMode.threadMode.MAIN)
    public boolean onTCourierSubscribeEvent(EventCourier msg) {
        MMLog.d(TAG,msg.toStr());
        switch (msg.getId()) {
            case OctopusUtils.DOWNLOAD_MESSAGE_SUCCESSFUL:///从网络下载完成后自动静默安装
                //Toast.makeText(this, "下载成功，开始静默安装", Toast.LENGTH_SHORT).show();
                MMLog.d(TAG,"下载完成，开始静默安装 "+msg.getObj().toString());
                OctopusUtils.startSilentInstallApk(this, msg.getObj().toString());
        }
        return true;
    }
}