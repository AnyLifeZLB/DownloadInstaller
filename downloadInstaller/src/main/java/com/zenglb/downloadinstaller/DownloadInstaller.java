package com.zenglb.downloadinstaller;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
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
 * 6.异常处理完善
 */
public class DownloadInstaller {
    private static final String id = "update_chanel_id_1";
    private static final String authority = "com.zenglb.downloadinstaller.fileprovider";
    private static final String intentType = "application/vnd.android.package-archive";

    private NotificationManager notificationManager;
    private Notification notification;
    private NotificationCompat.Builder builder;
    private static boolean isUpdate = false;
    // TODO: 2019-05-07 need  
    //是否有已经下载好的App，目前并不能知道是否用户真的安装好了新版本的App
    private static boolean haveUninstallApp = false;

    private Context mContext;
    private int progress;
    private int oldProgress;

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
        storageApkPath = Environment.getExternalStorageDirectory().getPath() + "/" + AppUtils.getAppName(mContext) + ".apk";
    }


    /**
     * app下载升级管理,builder
     *
     * @param apkUrl apk 下载地址
     */
    public void download(String apkUrl) {
        downloadApkUrl = apkUrl;
        if(!haveUninstallApp&&!isUpdate){
            initNotification();

            //如果没有正在下载&&没有下载好了还没有升级的
            new Thread(mDownApkRunnable).start();
        }
    }


    /**
     * 下载线程,使用最原始的HttpURLConnection，减少依赖
     */
    private Runnable mDownApkRunnable = new Runnable() {
        @Override
        public void run() {
            isUpdate = true;

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
                        updateNotify(progress);
                        oldProgress = progress;
                    }
                    fos.write(buf, 0, byteCount);
                }

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
                //后面有时间再完善异常的处理
                if (e.toString().contains("Permission denied")) {
                    notifyError(getStringFrom(R.string.download_failure_storage_permission_deny));
                    toastError(R.string.download_failure_storage_permission_deny);
                } else {
                    notifyError(getStringFrom(R.string.apk_update_download_failed));
                    toastError(R.string.apk_update_download_failed);
                }
            } finally {
                //finally do something
                isUpdate = false;
            }
        }
    };


    /**
     * get String from id
     *
     * @param id res id
     * @return string
     * @throws Resources.NotFoundException
     */
    @NonNull
    public String getStringFrom(@StringRes int id) {
        return mContext.getResources().getString(id);
    }

    /**
     * Toast error message
     *
     * @param id res id
     */
    private void toastError(@StringRes int id)  {
        Looper.prepare();
        Toast.makeText(mContext, getStringFrom(id), Toast.LENGTH_LONG).show();
        Looper.loop();
    }


    /**
     * 安装过程处理
     *
     *
     */
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
            haveUninstallApp = false;
        }
    }


    /**
     * 跳转到安装apk的页面
     */
    private void installApk() {
        File apkFile = new File(storageApkPath);
        if (!apkFile.exists()) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri contentUri = FileProvider.getUriForFile(mContext, authority, apkFile);
            intent.setDataAndType(contentUri, intentType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            intent.setDataAndType(Uri.parse("file://" + apkFile.toString()), intentType);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }


    /**
     * 初始化通知 initNotification
     */
    private void initNotification() {
        notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
//        NotificationChannel mChannel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW);
//        notificationManager.createNotificationChannel(mChannel);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(id, AppUtils.getAppName(mContext), NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(mChannel);
        }

        builder = new NotificationCompat.Builder(mContext, id);
        builder.setContentTitle(mContext.getResources().getString(R.string.apk_update_tips_title) + AppUtils.getAppName(mContext)) //设置通知标题
                .setSmallIcon(R.drawable.download)
                .setDefaults(Notification.DEFAULT_LIGHTS) //设置通知的提醒方式： 呼吸灯
                .setPriority(NotificationCompat.PRIORITY_MAX) //设置通知的优先级：最大
                .setAutoCancel(false)
                .setContentText(mContext.getResources().getString(R.string.apk_update_downloading_progress))
                .setChannelId(id)
                .setProgress(100, 0, false);
        notification = builder.build();//构建通知对象
    }

    /**
     * 通知下载更新过程中的错误信息
     *
     * @param errorMsg 错误信息
     */
    private void notifyError(String errorMsg) {
        builder.setContentTitle(mContext.getResources().getString(R.string.apk_update_tips_error_title));
        builder.setProgress(100, 0, false);
        builder.setContentText(errorMsg);
        notification = builder.build();
        notificationManager.notify(10086, notification);
    }



    /**
     * 更新下载的进度
     *
     * @param progress
     */
    private void updateNotify(int progress) {
        builder.setProgress(100, progress, false);
        builder.setContentText(mContext.getResources().getString(R.string.apk_update_downloading_progress) + " 「" + progress + "%」");
        notification = builder.build();

        //点击通知栏到安装界面，可能下载好了，用户没有安装
        if (progress == 100) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri contentUri = FileProvider.getUriForFile(mContext, authority, new File(storageApkPath));
                intent.setDataAndType(contentUri, intentType);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                intent.setDataAndType(Uri.parse("file://" + new File(storageApkPath).toString()), intentType);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            notification.contentIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
        }

        notificationManager.notify(10086, notification);
    }


}
