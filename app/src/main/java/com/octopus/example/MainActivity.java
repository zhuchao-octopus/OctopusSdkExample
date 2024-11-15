package com.octopus.example;

import static com.zhuchao.android.fbase.DataID.DEVICE_EVENT_READ;
import static com.zhuchao.android.fbase.DataID.DEVICE_EVENT_UART_READ;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.octopus.example.databinding.ActivityMainBinding;
import com.zhuchao.android.fbase.ByteUtils;
import com.zhuchao.android.fbase.DataID;
import com.zhuchao.android.fbase.EventCourier;
import com.zhuchao.android.fbase.FileUtils;
import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.fbase.MethodThreadMode;
import com.zhuchao.android.fbase.TCourierSubscribe;
import com.zhuchao.android.fbase.ThreadUtils;
import com.zhuchao.android.fbase.eventinterface.EventCourierInterface;
import com.zhuchao.android.net.NetworkInformation;
import com.zhuchao.android.player.AudioPlayer;
import com.zhuchao.android.serialport.TUartFile;
import com.zhuchao.android.session.Cabinet;
import com.zhuchao.android.session.base.BaseActivity;
import com.zhuchao.android.utils.TelephonyUtils;

import java.util.Objects;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private final String TAG = "MainActivity";
    private final AudioPlayer mAudioPlayer = new AudioPlayer();
    private ActivityMainBinding binding;
    private TUartFile tUartDevice;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (("android.intent.action.USER_DATA").equals(intent.getAction())) {
                int cmd = (int) intent.getIntExtra("cmd", 0);
                byte[] data = Objects.requireNonNull(intent.getExtras()).getByteArray("data");
                if (data != null)
                    MMLog.d(TAG, "USER_DATA cmd:" + cmd + ",data:" + ByteUtils.BuffToHexStr(data));
                else
                    MMLog.d(TAG, "USER_DATA cmd:" + cmd + ",data:" + null);
            }
        }
    };

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

        Cabinet.getDeviceManager().printAllDevice();
        Cabinet.getNet(this).registerNetStatusCallback(this);

        binding.button1.setOnClickListener(this);
        binding.button2.setOnClickListener(this);
        binding.button3.setOnClickListener(this);
        binding.button4.setOnClickListener(this);
        binding.button5.setOnClickListener(this);
        binding.button6.setOnClickListener(this);
        binding.button7.setOnClickListener(this);
        binding.button8.setOnClickListener(this);
        binding.button9.setOnClickListener(this);
        binding.button10.setOnClickListener(this);
        binding.button11.setOnClickListener(this);
        binding.button12.setOnClickListener(this);
        binding.button13.setOnClickListener(this);

        registerMyReceiver();
    }

    @SuppressLint("SdCardPath")
    @Override
    public void onClick(View v) {
        byte[] data = new byte[6];
        data[1] = 0x10;
        int id = v.getId();
        if (id == R.id.button1) {
            String apkUrl = "http://www.1234998.top/downloads/app-gps-demo.apk"; //需要从网络下载的APK URL地址
            OctopusUtils.startDownloadApkFrom(apkUrl, FileUtils.getDownloadDir(null));
        } else if (id == R.id.button2)///用户点击事件安装指定APK
        {
            String apkFileName = "/storage/emulated/0/Download/app-gps-demo.apk";//要静默安装的APK 完成路径名称
            OctopusUtils.startSilentInstallApk(this, apkFileName);
        } else if (id == R.id.button3) {
            String appPackageName = "";//要卸载的APP包名
            OctopusUtils.startSilentUninstallApk(this, appPackageName);
        } else if (id == R.id.button4) {
            this.hideStatusBar();
            recreate();
        } else if (id == R.id.button5) {
            this.showStatusBar();
            ThreadUtils.runThread(new Runnable() {
                @Override
                public void run() {
                    recreate();
                }
            }, 500);

        } else if (id == R.id.button6) {
            this.startLocalActivity(FullscreenActivity.class);
        } else if (id == R.id.button7) {
            tUartDevice = Cabinet.getUartDevice("/dev/ttyS0", 115200);
            if (tUartDevice != null) {
                tUartDevice.clearFrameStartCode();
                tUartDevice.clearFrameEndCode();
                tUartDevice.setFrameMiniSize(1);
                //tUartDevice.setDebug(true);
                //tUartDevice.addFrameStartCode(0xA100);
                //注册设备事件总线,异步处理设备写入操作
                Cabinet.getEventBus().registerEventObserver(tUartDevice.getDeviceTag(), tUartDevice);
                //启动设备开始读取数据
                tUartDevice.startPollingRead();
                //注册串口接收数据回调方法
                tUartDevice.registerOnReceivedCallback(this);
            }
        } else if (id == R.id.button8) {
            if (tUartDevice != null) tUartDevice.writeBytesWait(data);
        } else if (id == R.id.button9) {
            if (tUartDevice != null) {
                Cabinet.getEventBus().post(new EventCourier(tUartDevice.getDeviceTag(), DataID.DEVICE_EVENT_UART_WRITE, data));
            }
        } else if (id == R.id.button10) {
            Cabinet.getTTS(this).speak("hello! are you ok!");
        } else if (id == R.id.button11) {
            FileUtils.getFileSystemPartitions(null).printAll();
            Cabinet.getNet(this).printNetworkInformation();
            MMLog.d(TAG, "IMEI  :" + TelephonyUtils.getIMEI(this));
            MMLog.d(TAG, "ICCID :" + TelephonyUtils.getICCID(this));
        } else if (id == R.id.button12) {
            if (mAudioPlayer != null) {
                byte[] pcmData = mAudioPlayer.loadPCMFile("/sdcard/carplay_jalt_play.pcm");
                if (pcmData != null) mAudioPlayer.play(pcmData);
            }
        } else if (id == R.id.button13) {
            //OMedia oMedia = new OMedia("/sdcard/carplay_jalt_play.pcm");
            //oMedia.with(this).setMagicNumber(0).play();
            //String systemuIType = MachineConfig.getPropertyReadOnly("timezone");
            //MMLog.d( "TAG",  "androidupdate: systemuIType="+ systemuIType);
            //TAppProcessUtils.killApplication(this,"com.carletter.car");
            //TAppProcessUtils.killApplication(this,"com.carletter.car");
            startRemoteActivity("com.octopus.android.carapps", "com.octopus.android.carapps.radio.RadioActivity");
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerMyReceiver() {
        IntentFilter filter = new IntentFilter();//创建IntentFilter对象
        filter.addAction("android.intent.action.USER_DATA");
        filter.addAction(Intent.ACTION_SCREEN_OFF);//IntentFilter对象中添加要接收的关屏广播
        filter.addAction(Intent.ACTION_SCREEN_ON);//添加点亮屏幕广播
        registerReceiver(broadcastReceiver, filter);
    }

    private void unRegisterMyReceiver() {
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);//反注册广播，也就是注销广播接收者，使其不起作用
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tUartDevice != null) {
            tUartDevice.closeDevice();
        }
        unRegisterMyReceiver();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        MMLog.d(TAG, "onConfigurationChanged " + newConfig.toString());
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
    //自定义事件监听
    @TCourierSubscribe(threadMode = MethodThreadMode.threadMode.MAIN)
    public boolean onTCourierSubscribeEvent(EventCourier msg) {
        MMLog.d(TAG, msg.toStr());
        switch (msg.getId()) {
            case OctopusUtils.DOWNLOAD_MESSAGE_SUCCESSFUL:///从网络下载完成后自动静默安装
                //Toast.makeText(this, "下载成功，开始静默安装", Toast.LENGTH_SHORT).show();
                MMLog.d(TAG, "下载完成，开始静默安装 " + msg.getObj().toString());
                OctopusUtils.startSilentInstallApk(this, msg.getObj().toString());
        }
        return true;
    }

    //串口设备总线事件监听，接收数据
    @Override
    public boolean onCourierEvent(EventCourierInterface eventCourier) {
        MMLog.d(TAG, eventCourier.toStr());
        switch (eventCourier.getId()) {
            case DEVICE_EVENT_READ:
            case DEVICE_EVENT_UART_READ:
                //打印接收到的串口数据
                MMLog.d(TAG, ByteUtils.BuffToHexStr(eventCourier.getDatas()));
                break;
        }
        return true;
    }

    //网络状态事件监听
    @Override
    public void onNetStatusChanged(NetworkInformation networkInformation) {
        super.onNetStatusChanged(networkInformation);
        MMLog.d(TAG, networkInformation.toJson());//打印网络设备信息
    }
}