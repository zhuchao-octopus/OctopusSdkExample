package com.octopus.example;

import android.content.Context;
import android.content.Intent;

import com.zhuchao.android.fbase.DataID;
import com.zhuchao.android.fbase.EventCourier;
import com.zhuchao.android.fbase.FileUtils;
import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.fbase.TTask;
import com.zhuchao.android.fbase.eventinterface.TaskCallback;
import com.zhuchao.android.session.Cabinet;
import com.zhuchao.android.session.TTaskManager;

public class OctopusUtils {
    private static final String TAG = "OctopusUtils";
    public static final int  DOWNLOAD_MESSAGE_SUCCESSFUL = 10000;

    public static void startSilentInstallApk(Context context, String apkFilePathName) {
        if (!FileUtils.existFile(apkFilePathName)) {
            MMLog.log(TAG, apkFilePathName + " not exists");
            return;
        }
        if (!apkFilePathName.endsWith(".apk")) {
            MMLog.log(TAG, apkFilePathName + " is invalid");
            return;
        }

        Intent intent = new Intent("android.intent.action.SILENT_INSTALL_PACKAGE");
        intent.putExtra("apkFilePathName", apkFilePathName);
        intent.putExtra("installedReboot", false);//安装成功后是否重启设备，true为启动、false为不启动，默认为false;
        intent.putExtra("installedAutoStart", false); //安装成功后是否立刻启动，true为启动、false为不启动，默认为false;
        intent.putExtra("installedDeleteFile", true);////安装成功后是否删除apk,true表示删除 默认为false;

        if (context != null) {
            MMLog.log(TAG, "启动静默升级 ————>" + apkFilePathName);
            context.sendBroadcast(intent);
        } else {
            MMLog.log(TAG, "getApplicationContext() == null" + apkFilePathName);
        }
    }

    public static void startSilentUninstallApk(Context context,String packageName) {
        Intent intent = new Intent("android.intent.action.SILENT_UNINSTALL_PACKAGE");
        intent.putExtra("packageName", packageName);
        if (context != null) {
            MMLog.log(TAG, "启动静默卸载 ————>" + packageName);
            context.sendBroadcast(intent);
        }
    }

    public static void startDownloadApkFrom(Context context,String url,String toPath) {
        TTaskManager.dl(url,toPath).callbackHandler(new TaskCallback() {
            @Override
            public void onEventTaskFinished(Object obj, int status) {
                TTask tTask = (TTask) obj;
                String downloadPath = tTask.getProperties().getString("localPathFileName");
                switch (status) {
                    case DataID.TASK_STATUS_SUCCESS:
                        MMLog.log(TAG, "Download successful localPathFileName = " + downloadPath);
                        Cabinet.getEventBus().postMain(new EventCourier("MainActivity",DOWNLOAD_MESSAGE_SUCCESSFUL, null));
                        break;
                    case DataID.TASK_STATUS_PROGRESSING:
                        //Cabinet.getEventBus().postMain(new EventCourier("DownloadFragment", DataID.TASK_STATUS_PROGRESSING, tTask));
                        MMLog.d(TAG,"downloadPath="+downloadPath+":"+tTask.getProperties().getString("progress"));
                        break;
                }
            }
        }).start();
    }

}
