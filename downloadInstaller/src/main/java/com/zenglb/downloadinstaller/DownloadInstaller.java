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
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.net.UnknownServiceException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import com.zdf.activitylauncher.ActivityLauncher;


/**
 * App 下载升级管理器.单线程稳定，多线程下载异常多！！！
 * <p>
 * https://bintray.com/anylifezlb
 * <p>
 * 5.新的进程处理？app 杀了也没有关系
 * <p>
 * 7.安装时候APK MD5 检查，断点续传，多线程下载
 * <p>
 * https://github.com/miomin/Multiple-ChannelResumeDownloader
 * https://github.com/yaowen369/DownloadHelper
 */
public class DownloadInstaller {
    private String applicationID ;

    private String authority ;
    private static final String intentType = "application/vnd.android.package-archive";

    private NotificationManager notificationManager;
    private Notification notification;
    private NotificationCompat.Builder builder;

    private Context mContext;
    private int progress;
    private int oldProgress;

    //新包的下载地址
    private String downloadApkUrl;
    private String downloadApkUrlMd5;
    private int downloadApkNotifyId;

    //local saveFilePath
    private String storageApkPath;

    //事件监听器
    private DownloadProgressCallBack downloadProgressCallBack;

    //保存下载状态信息，临时过度的方案。后期多线程断点续方案就要使用数据库了
    private static HashMap<String, Integer> hashMap = new HashMap();

    /**
     * 不需要下载进度回调的
     *
     * @param context        上下文
     * @param downloadApkUrl apk 下载地址
     */
    public DownloadInstaller(Context context, String downloadApkUrl) {
        this(context, downloadApkUrl, null);
    }


    /**
     * 需要下载进度回调的
     *
     * @param context                  上下文
     * @param downloadApkUrl           apk下载地址
     * @param callBack 进度状态回调
     */
    public DownloadInstaller(Context context, String downloadApkUrl, DownloadProgressCallBack callBack) {
        this.mContext = context;
        this.downloadApkUrl = downloadApkUrl;
        this.downloadProgressCallBack = callBack;
    }


    /**
     * 获取16位的MD5 值，大写
     *
     * @param str
     * @return
     */
    private String getUpperMD5Str16(String str) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgorithmException caught!");
            System.exit(-1);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] byteArray = messageDigest.digest();
        StringBuffer md5StrBuff = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
                md5StrBuff.append("0").append(
                        Integer.toHexString(0xFF & byteArray[i]));
            else
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
        }
        return md5StrBuff.toString().toUpperCase().substring(8, 24);
    }


    /**
     * app下载升级管理
     */
    public void start() {
        applicationID=mContext.getPackageName();

        //防止不同的app 下载同一个链接的App 失败
        downloadApkUrlMd5 = getUpperMD5Str16(downloadApkUrl+applicationID);
        downloadApkNotifyId = downloadApkUrlMd5.hashCode();

        //https://developer.android.com/studio/build/application-id?hl=zh-cn
        authority=applicationID+".fileProvider";
        storageApkPath = Environment.getExternalStorageDirectory().getPath() + "/" + AppUtils.getAppName(mContext) + downloadApkUrlMd5 + ".apk";

        Integer downloadStatus = hashMap.get(downloadApkUrlMd5);

        //若果发现有已经下载好的文件，MD5 也是一样的话。 进度直接变成 100%
        if (downloadStatus == null || downloadStatus == DownloadInstallStatus.UN_DOWNLOAD || downloadStatus == DownloadInstallStatus.DOWNLOAD_ERROR) {
            initNotification();
            //如果没有正在下载&&没有下载好了还没有升级
            new Thread(mDownApkRunnable).start();
        }

    }


    /**
     * 下载线程,使用最原始的HttpURLConnection，减少依赖
     * 大的APK下载还是比较慢的，后面改为多线程下载
     *
     */
    private Runnable mDownApkRunnable = new Runnable() {
        @Override
        public void run() {
            hashMap.put(downloadApkUrlMd5, DownloadInstallStatus.DOWNLOADING);
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
                        if (downloadProgressCallBack != null) {
                            downloadProgressCallBack.downloadProgress(progress);
                        }
                        oldProgress = progress;
                    }
                    fos.write(buf, 0, byteCount);
                }

                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hashMap.put(downloadApkUrlMd5, DownloadInstallStatus.UNINSTALL);
                        installProcess();
                    }
                });

                fos.flush();
                fos.close();
                is.close();

            } catch (Exception e) {
                hashMap.put(downloadApkUrlMd5, DownloadInstallStatus.DOWNLOAD_ERROR);

                if (downloadProgressCallBack != null) {
                    downloadProgressCallBack.downloadException(e);
                }

                //后面有时间再完善异常的处理
                if (e instanceof FileNotFoundException) {
                    notifyError(getStringFrom(R.string.download_failure_file_not_found));
                    toastError(R.string.download_failure_file_not_found);
                } else if (e instanceof ConnectException) {
                    notifyError(getStringFrom(R.string.download_failure_net_deny));
                    toastError(R.string.download_failure_net_deny);
                } else if (e instanceof UnknownHostException) {
                    notifyError(getStringFrom(R.string.download_failure_net_deny));
                    toastError(R.string.download_failure_net_deny);
                } else if (e instanceof UnknownServiceException) {
                    notifyError(getStringFrom(R.string.download_failure_net_deny));
                    toastError(R.string.download_failure_net_deny);
                } else if (e.toString().contains("Permission denied")) {
                    notifyError(getStringFrom(R.string.download_failure_storage_permission_deny));
                    toastError(R.string.download_failure_storage_permission_deny);
                } else {
                    notifyError(getStringFrom(R.string.apk_update_download_failed));
                    toastError(R.string.apk_update_download_failed);
                }

            } finally {
                //finally do something
            }
        }
    };


    /**
     * get String from id
     *
     * @param id res id
     * @return string
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
    private void toastError(@StringRes int id) {
        Looper.prepare();
        Toast.makeText(mContext, getStringFrom(id), Toast.LENGTH_LONG).show();
        Looper.loop();
    }


    /**
     * 安装过程处理
     *
     */
    public void installProcess() {
        if (progress < 100) {
            return;
        }

        if (Build.VERSION.SDK_INT >= 26) {
            boolean canInstallPackage = mContext.getPackageManager().canRequestPackageInstalls();
            final Integer downloadStatus = hashMap.get(downloadApkUrlMd5); //unboxing

            if (canInstallPackage) {
                if (downloadStatus == DownloadInstallStatus.UNINSTALL) {
                    installApk();
                    hashMap.put(downloadApkUrlMd5, DownloadInstallStatus.UN_DOWNLOAD);
                }
            } else {
                Uri packageURI = Uri.parse("package:" + AppUtils.getPackageName(mContext));
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);

                //检查是否可以安装未知来源的应用，没有权限就一直去尝试，我感觉这样子是很流氓的...
                //在这里拦截OnActivityResult,不要代码割裂
                ActivityLauncher.init((Activity) mContext).startActivityForResult(intent, new ActivityLauncher.Callback() {
                    @Override
                    public void onActivityResult(int resultCode, Intent data) {
                        if (downloadStatus == DownloadInstallStatus.UNINSTALL) {
                            installProcess();
                        }
                    }
                });

            }
        } else {
            installApk();
            hashMap.put(downloadApkUrlMd5, DownloadInstallStatus.UN_DOWNLOAD);
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

        /**
         * 开始安装了
         */
        if (downloadProgressCallBack != null) {
            downloadProgressCallBack.onInstallStart();
        }
    }


    /**
     * 初始化通知 initNotification
     */
    private void initNotification() {
        notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(downloadApkUrlMd5, downloadApkUrlMd5, NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(mChannel);
        }

        builder = new NotificationCompat.Builder(mContext, downloadApkUrl);
        builder.setContentTitle(mContext.getResources().getString(R.string.apk_update_tips_title) + AppUtils.getAppName(mContext)) //设置通知标题
                .setSmallIcon(R.drawable.download)
                .setDefaults(Notification.DEFAULT_LIGHTS) //设置通知的提醒方式： 呼吸灯
                .setPriority(NotificationCompat.PRIORITY_MAX) //设置通知的优先级：最大
                .setAutoCancel(true)  //
                .setOngoing(true)     // 不可以删除
                .setContentText(mContext.getResources().getString(R.string.apk_update_downloading_progress))
                .setChannelId(downloadApkUrlMd5)
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
        builder.setContentText(errorMsg);
        notification = builder.build();
        notificationManager.notify(downloadApkNotifyId, notification);
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
        notificationManager.notify(downloadApkNotifyId, notification);
    }


}
