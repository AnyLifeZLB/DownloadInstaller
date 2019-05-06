package com.zenglb.downloadinstaller;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.zdf.activitylauncher.ActivityLauncher;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * App 下载升级管理器
 * <p>
 * 1.wifi 环境下静默下载，否则询问用户是否下载
 * 2.强制更新的话就告知用户要强制下载，不同意就退出App
 * 3.首次安装妥善处理Android 8的未知来源安装问题（处理OnActivityResult 问题）
 * 4.Android 7以上的FileProvider 问题
 * 5.新的进程处理？app 杀了也没有关系
 */
public class DownloadInstaller {
    private Context mContext;
    private int progress;
    private int oldProgress;
    private NotificationManager notificationManager;

    //新包的下载地址
    private String downloadApkUrl;

    //local saveFilePath
    private String storageApkPath;


    /**
     * 构造方法
     *
     * @param context context
     */
    public DownloadInstaller(Context context) {
        this.mContext = context;
        notificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        storageApkPath = Environment.getExternalStorageDirectory().getPath() + "/" + AppUtils.getAppName(mContext) + ".apk";
    }


    /**
     * app下载升级管理,builder
     *
     * @param apkUrl apk 下载地址
     */
    public void download(String apkUrl) {
        downloadApkUrl = apkUrl;
        //开始下载线程
        new Thread(mDownApkRunnable).start();
    }


    /**
     * 下载线程,使用最原始的HttpURLConnection，减少依赖
     *
     */
    private Runnable mDownApkRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                URL url = new URL(downloadApkUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                int length = conn.getContentLength();
                InputStream is = conn.getInputStream();

                File file = new File(Environment.getExternalStorageDirectory().getPath());
                if (!file.exists()) {
                    file.mkdir();
                }

                File ApkFile = new File(storageApkPath);
                FileOutputStream fos = new FileOutputStream(ApkFile);

                int count = 0;
                byte buf[] = new byte[2048];
                int byteCount;

                while ((byteCount = is.read(buf)) > 0) {
                    count += byteCount;
                    progress = (int) (((float) count / length) * 100);
                    if (progress > oldProgress) {
                        updateNotify(mContext.getResources().getString(R.string.apk_update_downloading_title) + "  [" + progress + "%]");
                        oldProgress = progress;
                    }
                    fos.write(buf, 0, byteCount);
                }

                updateNotify(mContext.getResources().getString(R.string.download_success) + "  [" + progress + "%]");

                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        haveUninstallApp = true;
                        installProcess();
                    }
                });

                fos.flush();
                fos.close();
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
                if (e.toString().contains("Permission denied")) {
                    updateNotify(mContext.getResources().getString(R.string.download_failure_storage_permission_deny));

                    Looper.prepare();
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.download_failure_storage_permission_deny), Toast.LENGTH_LONG).show();
                    Looper.loop();
                } else {
                    updateNotify(mContext.getResources().getString(R.string.apk_update_download_failed));
                }
            } finally {
                // finally

            }
        }
    };


    //是否有已经下载好的App
    public static boolean haveUninstallApp = false;

    public void installProcess() {
        if (progress < 100) {
            return;
        }

        if (Build.VERSION.SDK_INT >= 26) {
            boolean canInstallPackage = mContext.getPackageManager().canRequestPackageInstalls();
            if (canInstallPackage) {
                if (haveUninstallApp) {
                    installApk();
                    haveUninstallApp = false;
                }
            } else {
                Uri packageURI = Uri.parse("package:" + AppUtils.getPackageName(mContext));
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
                //检查是否可以安装未知来源的应用，没有权限就一直去尝试，我感觉这样子是很流氓的...
                //在这里拦截OnActivityResult,不要代码割裂
                ActivityLauncher.init((Activity) mContext).startActivityForResult(intent, new ActivityLauncher.Callback() {
                    @Override
                    public void onActivityResult(int resultCode, Intent data) {
                        if (haveUninstallApp) {
                            installProcess();
                        }
                    }
                });
            }
        } else {
            installApk();
        }
    }


    /**
     * 跳转到安装apk的页面
     *
     */
    private void installApk() {
        File apkFile = new File(storageApkPath);
        if (!apkFile.exists()) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri contentUri = FileProvider.getUriForFile(mContext, "com.zenglb.downloadinstaller.fileprovider", apkFile);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            intent.setDataAndType(Uri.parse("file://" + apkFile.toString()), "application/vnd.android.package-archive");
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }


    /**
     * 更新通知
     *
     * @param notifyContent 通知内容
     */
    private void updateNotify(String notifyContent) {
        String id = "update_chanel_id_1";
        String name = "update_name";
        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(mChannel);
            notification = new Notification.Builder(mContext, AppUtils.getAppName(mContext))
                    .setChannelId(id)
                    .setContentTitle(mContext.getResources().getString(R.string.apk_update_tips_title))
                    .setContentText(notifyContent)
                    .setSmallIcon(R.drawable.download)
                    .build();
        } else {
            notification = new NotificationCompat.Builder(mContext, AppUtils.getAppName(mContext))
                    .setContentTitle(mContext.getResources().getString(R.string.apk_update_tips_title))
                    .setContentText(notifyContent)
                    .setSmallIcon(R.drawable.download)
                    .setOngoing(true)
                    .build();
        }

        //点击通知栏到安装界面，可能下载好了，用户没有安装
        if (notifyContent.contains("100")) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri contentUri = FileProvider.getUriForFile(mContext, "com.zenglb.downloadinstaller.fileprovider", new File(storageApkPath));
                intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                intent.setDataAndType(Uri.parse("file://" + new File(storageApkPath).toString()), "application/vnd.android.package-archive");
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            notification.contentIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
        }

        notificationManager.notify(10086, notification);
    }


}
